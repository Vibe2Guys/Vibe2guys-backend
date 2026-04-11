#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NETWORK_NAME="vibe2guys-backend_default"
APP_BASE_URL="http://app:8080"
POSTGRES_CONTAINER="vibe2guys-backend-postgres-1"
CURL_IMAGE="curlimages/curl:8.12.1"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

if ! docker compose --env-file "$ROOT_DIR/.env.local-docker" -f "$ROOT_DIR/compose.local.yml" ps app >/dev/null 2>&1; then
  echo "Docker stack is not running. Start it first." >&2
  exit 1
fi

BACKOFFICE_ACCESS_KEY="$(grep '^BACKOFFICE_ACCESS_KEY=' "$ROOT_DIR/.env.local-docker" | cut -d= -f2-)"
RUN_ID="$(date +%s)"
RESULT_FILE="$(mktemp)"
trap 'rm -f "$RESULT_FILE"' EXIT

record_pass() {
  printf 'PASS %s\n' "$1" >>"$RESULT_FILE"
}

record_fail() {
  printf 'FAIL %s :: %s\n' "$1" "$2" >>"$RESULT_FILE"
}

show_response_error() {
  local response="$1"
  jq -r '.message // .errorCode // "unknown error"' <<<"$response" 2>/dev/null || echo "$response"
}

api() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local data="${4:-}"
  shift 4 || true

  local args=(-sS -X "$method" "$APP_BASE_URL$path")
  if [[ -n "$token" ]]; then
    args+=(-H "Authorization: Bearer $token")
  fi
  if [[ -n "$data" ]]; then
    args+=(-H "Content-Type: application/json" --data "$data")
  fi
  while [[ $# -gt 0 ]]; do
    args+=(-H "$1")
    shift
  done

  docker run --rm --network "$NETWORK_NAME" "$CURL_IMAGE" "${args[@]}"
}

call_api() {
  local label="$1"
  local method="$2"
  local path="$3"
  local token="${4:-}"
  local data="${5:-}"
  shift 5 || true

  local response
  response="$(api "$method" "$path" "$token" "$data" "$@")"
  if jq -e '.success == true' >/dev/null 2>&1 <<<"$response"; then
    record_pass "$label"
  else
    record_fail "$label" "$(show_response_error "$response")"
  fi
  printf '%s' "$response"
}

require_success() {
  local label="$1"
  local response="$2"
  if ! jq -e '.success == true' >/dev/null 2>&1 <<<"$response"; then
    echo "Required step failed: $label" >&2
    echo "$response" >&2
    exit 1
  fi
}

sql() {
  docker exec "$POSTGRES_CONTAINER" psql -v ON_ERROR_STOP=1 -U vibe2guys -d vibe2guys -Atqc "$1"
}

register_user() {
  local name="$1"
  local email="$2"
  local password="$3"
  local response
  response="$(call_api "auth register $email" POST "/api/v1/auth/register" "" "$(jq -nc \
    --arg name "$name" \
    --arg email "$email" \
    --arg password "$password" \
    '{name:$name,email:$email,password:$password,role:"STUDENT"}')")"
  require_success "register $email" "$response"
}

login_user() {
  local email="$1"
  local password="$2"
  call_api "auth login $email" POST "/api/v1/auth/login" "" "$(jq -nc \
    --arg email "$email" \
    --arg password "$password" \
    '{email:$email,password:$password}')"
}

student1_email="student1-${RUN_ID}@example.com"
student2_email="student2-${RUN_ID}@example.com"
student3_email="student3-${RUN_ID}@example.com"
instructor_email="instructor-${RUN_ID}@example.com"
admin_email="admin-${RUN_ID}@example.com"
extra_email="extra-${RUN_ID}@example.com"

common_password="Password123!"

register_user "Student One" "$student1_email" "$common_password"
register_user "Student Two" "$student2_email" "$common_password"
register_user "Student Three" "$student3_email" "$common_password"
register_user "Instructor User" "$instructor_email" "$common_password"
register_user "Admin User" "$admin_email" "$common_password"

sql "update users set role='INSTRUCTOR' where email='${instructor_email}';"
sql "update users set role='ADMIN' where email='${admin_email}';"

student1_login="$(login_user "$student1_email" "$common_password")"
require_success "student1 login" "$student1_login"
student2_login="$(login_user "$student2_email" "$common_password")"
require_success "student2 login" "$student2_login"
student3_login="$(login_user "$student3_email" "$common_password")"
require_success "student3 login" "$student3_login"
instructor_login="$(login_user "$instructor_email" "$common_password")"
require_success "instructor login" "$instructor_login"
admin_login="$(login_user "$admin_email" "$common_password")"
require_success "admin login" "$admin_login"

student1_token="$(jq -r '.data.accessToken' <<<"$student1_login")"
student1_refresh="$(jq -r '.data.refreshToken' <<<"$student1_login")"
student1_id="$(jq -r '.data.user.userId' <<<"$student1_login")"
student2_token="$(jq -r '.data.accessToken' <<<"$student2_login")"
student2_id="$(jq -r '.data.user.userId' <<<"$student2_login")"
student3_token="$(jq -r '.data.accessToken' <<<"$student3_login")"
student3_id="$(jq -r '.data.user.userId' <<<"$student3_login")"
instructor_token="$(jq -r '.data.accessToken' <<<"$instructor_login")"
instructor_id="$(jq -r '.data.user.userId' <<<"$instructor_login")"
admin_token="$(jq -r '.data.accessToken' <<<"$admin_login")"

student_profile="$(call_api "users me student1" GET "/api/v1/users/me" "$student1_token" "")"
updated_profile="$(call_api "users me patch student1" PATCH "/api/v1/users/me" "$student1_token" "$(jq -nc \
  '{name:"Student One Updated",profileImageUrl:"https://example.com/avatar.png"}')")"

admin_users="$(call_api "admin users list" GET "/api/v1/backoffice/users?page=0&size=20" "$admin_token" "" "X-Backoffice-Key: $BACKOFFICE_ACCESS_KEY")"
admin_create_user="$(call_api "admin users create" POST "/api/v1/backoffice/users" "$admin_token" "$(jq -nc \
  --arg email "$extra_email" \
  --arg password "$common_password" \
  '{name:"Extra Student",email:$email,password:$password,role:"STUDENT"}')" "X-Backoffice-Key: $BACKOFFICE_ACCESS_KEY")"
admin_config="$(call_api "admin analytics-config get" GET "/api/v1/backoffice/analytics-config" "$admin_token" "" "X-Backoffice-Key: $BACKOFFICE_ACCESS_KEY")"
admin_config_patch="$(call_api "admin analytics-config patch" PATCH "/api/v1/backoffice/analytics-config" "$admin_token" "$(jq -nc \
  '{attendanceWeight:0.2,progressWeight:0.2,assignmentWeight:0.2,quizWeight:0.2,teamActivityWeight:0.2,riskThresholdHigh:80,riskThresholdMedium:55}')" "X-Backoffice-Key: $BACKOFFICE_ACCESS_KEY")"

course_create="$(call_api "courses create" POST "/api/v1/courses" "$instructor_token" "$(jq -nc \
  '{title:"API Smoke Course",description:"Full API smoke test course",thumbnailUrl:"https://example.com/course.png",startDate:"2026-04-01",endDate:"2026-06-30",isSequentialRelease:false}')")"
require_success "create course" "$course_create"
course_id="$(jq -r '.data.courseId' <<<"$course_create")"

course_update="$(call_api "courses patch" PATCH "/api/v1/courses/${course_id}" "$instructor_token" "$(jq -nc \
  '{description:"Updated for smoke test",status:"PUBLISHED"}')")"
courses_list_student="$(call_api "courses list student1" GET "/api/v1/courses?page=0&size=10" "$student1_token" "")"

enroll_student1="$(call_api "courses enroll student1" POST "/api/v1/courses/${course_id}/enrollments" "$student1_token" '{}')"
enroll_student2="$(call_api "courses enroll student2" POST "/api/v1/courses/${course_id}/enrollments" "$student2_token" '{}')"
enroll_student3="$(call_api "courses enroll student3" POST "/api/v1/courses/${course_id}/enrollments" "$student3_token" '{}')"

my_courses_student1="$(call_api "courses my student1" GET "/api/v1/courses/my" "$student1_token" "")"
course_detail_student1="$(call_api "course detail student1" GET "/api/v1/courses/${course_id}" "$student1_token" "")"
course_students="$(call_api "course students instructor" GET "/api/v1/courses/${course_id}/students?page=0&size=20" "$instructor_token" "")"

week_create="$(call_api "course week create" POST "/api/v1/courses/${course_id}/weeks" "$instructor_token" "$(jq -nc \
  '{weekNumber:1,title:"Week 1",openAt:"2026-04-01T09:00:00Z"}')")"
require_success "create week" "$week_create"
week_id="$(jq -r '.data.weekId' <<<"$week_create")"

content_create="$(call_api "content create" POST "/api/v1/weeks/${week_id}/contents" "$instructor_token" "$(jq -nc \
  '{type:"LIVE",title:"Intro Live Session",description:"Smoke test live content",videoUrl:"https://example.com/video.mp4",documentUrl:null,durationSeconds:1200,scheduledAt:"2026-04-11T00:00:00Z",openAt:"2026-04-01T09:00:00Z"}')")"
require_success "create content" "$content_create"
content_id="$(jq -r '.data.contentId' <<<"$content_create")"

assignment_create="$(call_api "assignment create" POST "/api/v1/courses/${course_id}/assignments" "$instructor_token" "$(jq -nc \
  '{title:"Smoke Assignment",description:"Submit a short answer",type:"DESCRIPTIVE",dueAt:"2026-12-31T23:59:59Z",teamAssignment:false}')")"
require_success "create assignment" "$assignment_create"
assignment_id="$(jq -r '.data.assignmentId' <<<"$assignment_create")"

quiz_create="$(call_api "quiz create" POST "/api/v1/courses/${course_id}/quizzes" "$instructor_token" "$(jq -nc \
  '{title:"Smoke Quiz",dueAt:"2026-12-31T23:59:59Z",questions:[{questionType:"MULTIPLE_CHOICE",questionText:"What is 2+2?",choices:["3","4","5"],answerKey:"4",score:10,sortOrder:1}]}')")"
require_success "create quiz" "$quiz_create"
quiz_id="$(jq -r '.data.quizId' <<<"$quiz_create")"

notifications_student1="$(call_api "notifications me student1" GET "/api/v1/notifications/me" "$student1_token" "")"
notification_id="$(jq -r '.data[0].notificationId // empty' <<<"$notifications_student1")"
if [[ -n "$notification_id" ]]; then
  notification_read="$(call_api "notifications read student1" PATCH "/api/v1/notifications/${notification_id}/read" "$student1_token" "")"
fi

week_contents_student1="$(call_api "course week contents student1" GET "/api/v1/courses/${course_id}/weeks/${week_id}/contents" "$student1_token" "")"
content_detail_student1="$(call_api "content detail student1" GET "/api/v1/contents/${content_id}" "$student1_token" "")"
attendance_start="$(call_api "attendance start student1" POST "/api/v1/contents/${content_id}/attendance" "$student1_token" "$(jq -nc '{enteredAt:"2026-04-11T01:00:00Z"}')")"
progress_save="$(call_api "content progress save student1" POST "/api/v1/contents/${content_id}/progress" "$student1_token" "$(jq -nc \
  '{watchedSeconds:600,totalSeconds:1200,lastPositionSeconds:600,replayCount:0,stoppedSegmentStart:540,stoppedSegmentEnd:600,eventType:"PAUSE"}')")"
progress_get="$(call_api "content progress get student1" GET "/api/v1/contents/${content_id}/progress" "$student1_token" "")"
attendance_end="$(call_api "attendance end student1" PATCH "/api/v1/contents/${content_id}/attendance" "$student1_token" "$(jq -nc '{leftAt:"2026-04-11T01:20:00Z"}')")"
learning_logs_me="$(call_api "course learning logs me student1" GET "/api/v1/courses/${course_id}/learning-logs/me" "$student1_token" "")"

assignment_list_student1="$(call_api "assignment list student1" GET "/api/v1/courses/${course_id}/assignments" "$student1_token" "")"
assignment_detail_student1="$(call_api "assignment detail student1" GET "/api/v1/assignments/${assignment_id}" "$student1_token" "")"
assignment_submit="$(call_api "assignment submit student1" POST "/api/v1/assignments/${assignment_id}/submissions" "$student1_token" "$(jq -nc \
  '{answerText:"This is my first answer.",fileUrls:["https://example.com/file.txt"]}')")"
submission_id="$(jq -r '.data.submissionId // empty' <<<"$assignment_submit")"
if [[ -n "$submission_id" ]]; then
  assignment_resubmit="$(call_api "assignment resubmit student1" PATCH "/api/v1/assignments/${assignment_id}/submissions/${submission_id}" "$student1_token" "$(jq -nc \
    '{answerText:"This is my updated answer.",fileUrls:["https://example.com/file2.txt"]}')")"
fi
assignment_submissions_instructor="$(call_api "assignment submissions instructor" GET "/api/v1/assignments/${assignment_id}/submissions" "$instructor_token" "")"

quiz_list_student1="$(call_api "quiz list student1" GET "/api/v1/courses/${course_id}/quizzes" "$student1_token" "")"
quiz_detail_student1="$(call_api "quiz detail student1" GET "/api/v1/quizzes/${quiz_id}" "$student1_token" "")"
question_id="$(jq -r '.data.questions[0].questionId' <<<"$quiz_detail_student1")"
quiz_submit="$(call_api "quiz submit student1" POST "/api/v1/quizzes/${quiz_id}/submissions" "$student1_token" "$(jq -nc \
  --argjson questionId "$question_id" \
  '{answers:[{questionId:$questionId,selectedChoice:"4"}]}')")"
quiz_result_me="$(call_api "quiz result me student1" GET "/api/v1/quizzes/${quiz_id}/results/me" "$student1_token" "")"

team_auto_grouping="$(call_api "team auto-grouping instructor" POST "/api/v1/courses/${course_id}/teams/auto-grouping" "$instructor_token" "$(jq -nc '{teamSize:2}')")"
course_teams="$(call_api "course teams instructor" GET "/api/v1/courses/${course_id}/teams" "$instructor_token" "")"
team_id="$(jq -r '.data[0].teamId' <<<"$course_teams")"
student3_team_me="$(call_api "teams me student3" GET "/api/v1/teams/me" "$student3_token" "")"
student3_team_id="$(jq -r '.data[0].teamId // empty' <<<"$student3_team_me")"
team_detail_student1="$(call_api "team detail student1" GET "/api/v1/teams/${team_id}" "$student1_token" "")"
team_chat_room="$(call_api "team chat-room student1" GET "/api/v1/teams/${team_id}/chat-room" "$student1_token" "")"
chat_room_id="$(jq -r '.data.chatRoomId' <<<"$team_chat_room")"
team_messages_before="$(call_api "team messages before student1" GET "/api/v1/chat-rooms/${chat_room_id}/messages" "$student1_token" "")"
team_message_create="$(call_api "team message create student1" POST "/api/v1/chat-rooms/${chat_room_id}/messages" "$student1_token" "$(jq -nc \
  '{messageBody:"Hello from smoke test"}')")"
team_messages_after="$(call_api "team messages after student1" GET "/api/v1/chat-rooms/${chat_room_id}/messages" "$student1_token" "")"
team_analytics="$(call_api "team analytics student1" GET "/api/v1/teams/${team_id}/analytics" "$student1_token" "")"
team_contributions="$(call_api "team contributions student1" GET "/api/v1/teams/${team_id}/members/contributions" "$student1_token" "")"
if [[ -n "$student3_id" ]]; then
  team_members_patch="$(call_api "team members patch instructor" PATCH "/api/v1/teams/${team_id}/members" "$instructor_token" "$(jq -nc \
    --argjson student3Id "$student3_id" \
    '{removeMemberIds:[],addMemberIds:[$student3Id]}')")"
fi

student_dashboard="$(call_api "analytics dashboard student" GET "/api/v1/dashboard/student" "$student1_token" "")"
student_report="$(call_api "analytics report me" GET "/api/v1/reports/me" "$student1_token" "")"
student_scores="$(call_api "analytics student scores" GET "/api/v1/students/${student1_id}/scores" "$student1_token" "")"
student_ai_understanding="$(call_api "analytics student ai-understanding" GET "/api/v1/students/${student1_id}/ai-understanding" "$student1_token" "")"
student_risk="$(call_api "analytics student risk" GET "/api/v1/students/${student1_id}/risk" "$student1_token" "")"
student_recommendations="$(call_api "analytics student recommendations" GET "/api/v1/students/${student1_id}/recommendations" "$student1_token" "")"

follow_up_question="$(call_api "ai follow-up question create" POST "/api/v1/ai/follow-up-questions" "$instructor_token" "$(jq -nc \
  --argjson courseId "$course_id" \
  --argjson contentId "$content_id" \
  --argjson studentId "$student1_id" \
  '{courseId:$courseId,contentId:$contentId,studentId:$studentId,contextType:"CONTENT",sourceText:"This content explained the difference between synchronous and asynchronous execution in a practical way."}')")"
require_success "follow-up question create" "$follow_up_question"
question_id_ai="$(jq -r '.data.questionId' <<<"$follow_up_question")"
follow_up_response="$(call_api "ai follow-up response create" POST "/api/v1/ai/follow-up-questions/${question_id_ai}/responses" "$student1_token" "$(jq -nc \
  '{answerText:"Asynchronous execution lets tasks continue without blocking, which improves responsiveness in UI and network-driven flows."}')")"
follow_up_analysis_student="$(call_api "ai follow-up analysis student" GET "/api/v1/ai/follow-up-questions/${question_id_ai}/analysis" "$student1_token" "")"

notifications_after_ai="$(call_api "notifications me after ai student1" GET "/api/v1/notifications/me" "$student1_token" "")"

instructor_dashboard="$(call_api "analytics instructor dashboard" GET "/api/v1/dashboard/instructor/courses/${course_id}" "$instructor_token" "")"
instructor_risk_students="$(call_api "analytics instructor risk students" GET "/api/v1/instructors/courses/${course_id}/students/risk" "$instructor_token" "")"
instructor_low_understanding="$(call_api "analytics instructor low understanding" GET "/api/v1/instructors/courses/${course_id}/students/understanding-low" "$instructor_token" "")"
instructor_student_detail="$(call_api "analytics instructor student detail" GET "/api/v1/instructors/courses/${course_id}/students/${student1_id}" "$instructor_token" "")"
instructor_interventions_before="$(call_api "analytics instructor interventions before" GET "/api/v1/instructors/courses/${course_id}/interventions" "$instructor_token" "")"
instructor_intervention_create="$(call_api "analytics instructor intervention create" POST "/api/v1/instructors/courses/${course_id}/interventions" "$instructor_token" "$(jq -nc \
  --argjson studentId "$student1_id" \
  '{studentId:$studentId,type:"COUNSELING",title:"Check-in",message:"Please review the topic and join office hours.",resourceUrls:["https://example.com/review"]}')")"
instructor_interventions_after="$(call_api "analytics instructor interventions after" GET "/api/v1/instructors/courses/${course_id}/interventions" "$instructor_token" "")"
instructor_score_distribution="$(call_api "analytics instructor score distribution" GET "/api/v1/instructors/courses/${course_id}/score-distribution" "$instructor_token" "")"

refresh_student1="$(call_api "auth refresh student1" POST "/api/v1/auth/refresh" "" "$(jq -nc --arg refreshToken "$student1_refresh" '{refreshToken:$refreshToken}')")"
require_success "refresh student1" "$refresh_student1"
student1_new_access="$(jq -r '.data.accessToken' <<<"$refresh_student1")"
student1_new_refresh="$(jq -r '.data.refreshToken' <<<"$refresh_student1")"
logout_student1="$(call_api "auth logout student1" POST "/api/v1/auth/logout" "$student1_new_access" "$(jq -nc --arg refreshToken "$student1_new_refresh" '{refreshToken:$refreshToken}')")"

printf '\nSmoke Results\n'
cat "$RESULT_FILE"
