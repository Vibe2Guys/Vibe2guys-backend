ALTER TABLE teams
    ADD COLUMN team_building_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN profile_diversity_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN matching_summary VARCHAR(500);

ALTER TABLE team_members
    ADD COLUMN learning_style VARCHAR(30) NOT NULL DEFAULT 'BALANCED',
    ADD COLUMN reliability_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN initiative_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN support_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN understanding_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN profile_summary VARCHAR(300);
