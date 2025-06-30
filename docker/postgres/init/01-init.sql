-- PostgreSQL database initialization script
-- This script runs when the PostgreSQL container starts for the first time

-- The database and user are already created by environment variables
-- This script can be used for additional database setup

-- Create any additional extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant necessary privileges
GRANT ALL PRIVILEGES ON DATABASE ordermanagement TO ordermanagement;
GRANT ALL PRIVILEGES ON SCHEMA public TO ordermanagement;

-- Log initialization
\echo 'PostgreSQL database initialized for Order Management System'