DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'difficultylevel') THEN
        CREATE DOMAIN difficultylevel AS difficulty_level;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'testsessionstatus') THEN
        CREATE DOMAIN testsessionstatus AS test_session_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'usersystemrole') THEN
        CREATE DOMAIN usersystemrole AS user_system_role;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'useroccupation') THEN
        CREATE DOMAIN useroccupation AS user_occupation;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'questiontype') THEN
        CREATE DOMAIN questiontype AS question_type;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lessonstatus') THEN
        CREATE DOMAIN lessonstatus AS lesson_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'submissionstatus') THEN
        CREATE DOMAIN submissionstatus AS submission_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sqltopiccode') THEN
        CREATE DOMAIN sqltopiccode AS sql_topic_code;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'activitytype') THEN
        CREATE DOMAIN activitytype AS activity_type;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'oauthprovider') THEN
        CREATE DOMAIN oauthprovider AS oauth_provider;
    END IF;
END $$;
