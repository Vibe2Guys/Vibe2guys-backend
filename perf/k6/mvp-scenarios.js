import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const studentToken = __ENV.STUDENT_ACCESS_TOKEN || '';
const instructorToken = __ENV.INSTRUCTOR_ACCESS_TOKEN || '';
const courseId = __ENV.COURSE_ID || '1';
const contentId = __ENV.CONTENT_ID || '1';
const chatRoomId = __ENV.CHAT_ROOM_ID || '1';

export const options = {
  scenarios: {
    course_reads: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      exec: 'courseReads'
    },
    progress_writes: {
      executor: 'constant-vus',
      vus: 20,
      duration: '30s',
      exec: 'progressWrites'
    },
    dashboard_reads: {
      executor: 'constant-vus',
      vus: 5,
      duration: '30s',
      exec: 'dashboardReads'
    },
    team_chat_writes: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      exec: 'teamChatWrites'
    }
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800']
  }
};

function authHeaders(token) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };
}

export function courseReads() {
  const res = http.get(`${baseUrl}/api/v1/courses/${courseId}`, authHeaders(studentToken));
  check(res, {
    'course read status 200': (r) => r.status === 200
  });
  sleep(1);
}

export function progressWrites() {
  const body = JSON.stringify({
    watchedSeconds: 120,
    totalSeconds: 1200,
    lastPositionSeconds: 120,
    replayCount: 0,
    stoppedSegmentStart: 110,
    stoppedSegmentEnd: 120,
    eventType: 'PAUSE'
  });
  const res = http.post(`${baseUrl}/api/v1/contents/${contentId}/progress`, body, authHeaders(studentToken));
  check(res, {
    'progress write status 200': (r) => r.status === 200
  });
  sleep(1);
}

export function dashboardReads() {
  const res = http.get(`${baseUrl}/api/v1/dashboard/instructor/courses/${courseId}`, authHeaders(instructorToken));
  check(res, {
    'dashboard read status 200': (r) => r.status === 200
  });
  sleep(1);
}

export function teamChatWrites() {
  const body = JSON.stringify({
    messageBody: 'load-test-message'
  });
  const res = http.post(`${baseUrl}/api/v1/chat-rooms/${chatRoomId}/messages`, body, authHeaders(studentToken));
  check(res, {
    'chat write status 200': (r) => r.status === 200
  });
  sleep(1);
}
