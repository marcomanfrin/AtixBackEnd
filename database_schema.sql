-- =====================================================
-- ATIX BACKEND - DATABASE SCHEMA
-- =====================================================
-- Database: PostgreSQL 14+
-- Version: 1.0
-- Created: 2024-01-15
-- =====================================================

-- =====================================================
-- DROP TABLES (in reverse order to respect FK constraints)
-- =====================================================

DROP TABLE IF EXISTS attachment_links CASCADE;
DROP TABLE IF EXISTS attachments CASCADE;
DROP TABLE IF EXISTS worksite_reference_assignments CASCADE;
DROP TABLE IF EXISTS worksite_references CASCADE;
DROP TABLE IF EXISTS work_assignments CASCADE;
DROP TABLE IF EXISTS work_report_entries CASCADE;
DROP TABLE IF EXISTS work_reports CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS works CASCADE;
DROP TABLE IF EXISTS plants CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- ENABLE UUID EXTENSION
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- TABLES
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- Description: Stores all user types (SINGLE_TABLE inheritance)
-- -----------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_type VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'OWNER')),

    CONSTRAINT chk_user_type CHECK (user_type IN ('ADMINISTRATION', 'TECHNICIAN', 'SELLER'))
);

COMMENT ON TABLE users IS 'Stores all user types with Single Table Inheritance strategy';
COMMENT ON COLUMN users.user_type IS 'Discriminator column for user type (ADMINISTRATION, TECHNICIAN, SELLER)';
COMMENT ON COLUMN users.role IS 'User role for authorization (USER, ADMIN, OWNER)';
COMMENT ON COLUMN users.password IS 'Hashed password using BCrypt';

-- -----------------------------------------------------
-- Table: clients
-- Description: Stores client information (ATIX or FINAL)
-- -----------------------------------------------------
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('ATIX', 'FINAL'))
);

COMMENT ON TABLE clients IS 'Stores client information (can be ATIX client or final client)';
COMMENT ON COLUMN clients.type IS 'Client type: ATIX (intermediary) or FINAL (end customer)';

-- -----------------------------------------------------
-- Table: plants
-- Description: Stores plant/facility information
-- -----------------------------------------------------
CREATE TABLE plants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    notes TEXT NOT NULL,
    nas_directory VARCHAR(255) NOT NULL,
    psw_phrase VARCHAR(255) NOT NULL,
    psw_platform VARCHAR(255) NOT NULL,
    psw_station VARCHAR(255) NOT NULL
);

COMMENT ON TABLE plants IS 'Stores plant/facility information with access credentials';
COMMENT ON COLUMN plants.nas_directory IS 'NAS directory path for plant files';
COMMENT ON COLUMN plants.psw_phrase IS 'Security phrase password';
COMMENT ON COLUMN plants.psw_platform IS 'Platform access password';
COMMENT ON COLUMN plants.psw_station IS 'Station access password';

-- -----------------------------------------------------
-- Table: works
-- Description: Main table for work orders/projects
-- -----------------------------------------------------
CREATE TABLE works (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    bid_number VARCHAR(50) NOT NULL,
    seller_id UUID,
    order_number VARCHAR(50) NOT NULL,
    order_date DATE NOT NULL,
    electrical_schema_progression INT NOT NULL DEFAULT 0 CHECK (electrical_schema_progression >= 0 AND electrical_schema_progression <= 100),
    programming_progression INT NOT NULL DEFAULT 0 CHECK (programming_progression >= 0 AND programming_progression <= 100),
    expected_start_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invoiced BOOLEAN NOT NULL DEFAULT FALSE,
    invoiced_at TIMESTAMP,
    plant_id UUID,
    atix_client_id UUID NOT NULL,
    final_client_id UUID,
    nas_sub_directory VARCHAR(255) NOT NULL,
    expected_office_hours INT,
    expected_plant_hours INT,

    CONSTRAINT fk_works_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_works_plant FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE SET NULL,
    CONSTRAINT fk_works_atix_client FOREIGN KEY (atix_client_id) REFERENCES clients(id) ON DELETE RESTRICT,
    CONSTRAINT fk_works_final_client FOREIGN KEY (final_client_id) REFERENCES clients(id) ON DELETE SET NULL
);

COMMENT ON TABLE works IS 'Main table for work orders and projects';
COMMENT ON COLUMN works.electrical_schema_progression IS 'Progress percentage for electrical schema (0-100)';
COMMENT ON COLUMN works.programming_progression IS 'Progress percentage for programming (0-100)';
COMMENT ON COLUMN works.completed IS 'Whether the work is marked as completed';
COMMENT ON COLUMN works.invoiced IS 'Whether the work has been invoiced';
COMMENT ON COLUMN works.nas_sub_directory IS 'Subdirectory in NAS for work files';

-- -----------------------------------------------------
-- Table: tickets
-- Description: Support tickets and issue tracking
-- -----------------------------------------------------
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_email VARCHAR(100),
    order_number_id UUID,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tickets_work FOREIGN KEY (order_number_id) REFERENCES works(id) ON DELETE SET NULL
);

COMMENT ON TABLE tickets IS 'Support tickets and issue tracking system';
COMMENT ON COLUMN tickets.status IS 'Ticket status: OPEN, IN_PROGRESS, RESOLVED, CLOSED';
COMMENT ON COLUMN tickets.order_number_id IS 'Optional reference to related work order';

-- -----------------------------------------------------
-- Table: work_reports
-- Description: Summary reports for works
-- -----------------------------------------------------
CREATE TABLE work_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_id UUID NOT NULL UNIQUE,
    total_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_work_reports_work FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE
);

COMMENT ON TABLE work_reports IS 'Summary reports for work orders containing total hours';
COMMENT ON COLUMN work_reports.total_hours IS 'Total hours accumulated from all entries';

-- -----------------------------------------------------
-- Table: work_report_entries
-- Description: Individual entries in work reports
-- -----------------------------------------------------
CREATE TABLE work_report_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL,
    description TEXT NOT NULL,
    hours DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_work_report_entries_report FOREIGN KEY (report_id) REFERENCES work_reports(id) ON DELETE CASCADE
);

COMMENT ON TABLE work_report_entries IS 'Individual time entries for work reports';
COMMENT ON COLUMN work_report_entries.hours IS 'Hours spent on this specific activity';

-- -----------------------------------------------------
-- Table: work_assignments
-- Description: Assignment of users (technicians) to works
-- -----------------------------------------------------
CREATE TABLE work_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_id UUID NOT NULL,
    user_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_work_assignments_work FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
    CONSTRAINT fk_work_assignments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_work_user UNIQUE (work_id, user_id)
);

COMMENT ON TABLE work_assignments IS 'Assignment of technicians/users to work orders';
COMMENT ON CONSTRAINT uk_work_user ON work_assignments IS 'Prevents duplicate assignments of same user to same work';

-- -----------------------------------------------------
-- Table: worksite_references
-- Description: External worksite references (contractors, etc.)
-- -----------------------------------------------------
CREATE TABLE worksite_references (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL
);

COMMENT ON TABLE worksite_references IS 'External worksite references like contractors, plumbers, electricians';

-- -----------------------------------------------------
-- Table: worksite_reference_assignments
-- Description: Assignment of worksite references to works with roles
-- -----------------------------------------------------
CREATE TABLE worksite_reference_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_id UUID NOT NULL,
    worksite_reference_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PLUMBER', 'ELECTRICIAN')),

    CONSTRAINT fk_wra_work FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
    CONSTRAINT fk_wra_reference FOREIGN KEY (worksite_reference_id) REFERENCES worksite_references(id) ON DELETE CASCADE,
    CONSTRAINT uk_work_worksite_reference UNIQUE (work_id, worksite_reference_id)
);

COMMENT ON TABLE worksite_reference_assignments IS 'Links worksite references to works with specific roles';
COMMENT ON COLUMN worksite_reference_assignments.role IS 'Role of the reference: PLUMBER, ELECTRICIAN';

-- -----------------------------------------------------
-- Table: attachments
-- Description: File attachments stored in Cloudinary
-- -----------------------------------------------------
CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE attachments IS 'File attachments stored in Cloudinary CDN';
COMMENT ON COLUMN attachments.public_id IS 'Cloudinary public_id for file management';
COMMENT ON COLUMN attachments.resource_type IS 'Cloudinary resource type (image, video, raw, etc.)';
COMMENT ON COLUMN attachments.type IS 'Application-level file type classification';

-- -----------------------------------------------------
-- Table: attachment_links
-- Description: Polymorphic links between attachments and entities
-- -----------------------------------------------------
CREATE TABLE attachment_links (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    attachment_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('WORK', 'PLANT', 'TICKET', 'REPORT')),
    target_id UUID NOT NULL,

    CONSTRAINT fk_attachment_links_attachment FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE,
    CONSTRAINT uk_attachment_target UNIQUE (attachment_id, target_type, target_id)
);

COMMENT ON TABLE attachment_links IS 'Polymorphic association table linking attachments to various entities';
COMMENT ON COLUMN attachment_links.target_type IS 'Type of entity: WORK, PLANT, TICKET, REPORT';
COMMENT ON COLUMN attachment_links.target_id IS 'UUID of the target entity';

-- =====================================================
-- INDEXES for Performance
-- =====================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_type ON users(user_type);
CREATE INDEX idx_users_role ON users(role);

-- Works indexes
CREATE INDEX idx_works_seller ON works(seller_id);
CREATE INDEX idx_works_plant ON works(plant_id);
CREATE INDEX idx_works_atix_client ON works(atix_client_id);
CREATE INDEX idx_works_final_client ON works(final_client_id);
CREATE INDEX idx_works_completed ON works(completed);
CREATE INDEX idx_works_invoiced ON works(invoiced);
CREATE INDEX idx_works_order_date ON works(order_date);
CREATE INDEX idx_works_created_at ON works(created_at);
CREATE INDEX idx_works_bid_number ON works(bid_number);
CREATE INDEX idx_works_order_number ON works(order_number);

-- Tickets indexes
CREATE INDEX idx_tickets_order_number ON tickets(order_number_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
CREATE INDEX idx_tickets_sender_email ON tickets(sender_email);

-- Work Reports indexes
CREATE INDEX idx_work_reports_work ON work_reports(work_id);

-- Work Report Entries indexes
CREATE INDEX idx_work_report_entries_report ON work_report_entries(report_id);

-- Work Assignments indexes
CREATE INDEX idx_work_assignments_work ON work_assignments(work_id);
CREATE INDEX idx_work_assignments_user ON work_assignments(user_id);

-- Worksite Reference Assignments indexes
CREATE INDEX idx_wra_work ON worksite_reference_assignments(work_id);
CREATE INDEX idx_wra_reference ON worksite_reference_assignments(worksite_reference_id);

-- Attachment Links indexes
CREATE INDEX idx_attachment_links_attachment ON attachment_links(attachment_id);
CREATE INDEX idx_attachment_links_target ON attachment_links(target_type, target_id);

-- =====================================================
-- SAMPLE DATA (Optional - for development/testing)
-- =====================================================

-- Uncomment to insert sample data

/*
-- Insert sample users
INSERT INTO users (id, user_type, first_name, last_name, email, password, role) VALUES
    (uuid_generate_v4(), 'ADMINISTRATION', 'Admin', 'User', 'admin@atix.com', '$2a$10$...', 'OWNER'),
    (uuid_generate_v4(), 'TECHNICIAN', 'Mario', 'Rossi', 'mario.rossi@atix.com', '$2a$10$...', 'USER'),
    (uuid_generate_v4(), 'SELLER', 'Laura', 'Bianchi', 'laura.bianchi@atix.com', '$2a$10$...', 'USER');

-- Insert sample clients
INSERT INTO clients (id, name, type) VALUES
    (uuid_generate_v4(), 'Atix Automazione SRL', 'ATIX'),
    (uuid_generate_v4(), 'Cliente Finale SPA', 'FINAL');

-- Insert sample plant
INSERT INTO plants (id, name, notes, nas_directory, psw_phrase, psw_platform, psw_station) VALUES
    (uuid_generate_v4(), 'Impianto Centrale Milano', 'Impianto principale', '/plants/milano', 'phrase123', 'platform456', 'station789');
*/

-- =====================================================
-- FUNCTIONS AND TRIGGERS (Optional)
-- =====================================================

-- Function to automatically update total_hours in work_reports
CREATE OR REPLACE FUNCTION update_work_report_total_hours()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE work_reports
    SET total_hours = (
        SELECT COALESCE(SUM(hours), 0)
        FROM work_report_entries
        WHERE report_id = COALESCE(NEW.report_id, OLD.report_id)
    )
    WHERE id = COALESCE(NEW.report_id, OLD.report_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Trigger to update total hours when entries are added/updated/deleted
CREATE TRIGGER trg_update_work_report_total_hours
AFTER INSERT OR UPDATE OR DELETE ON work_report_entries
FOR EACH ROW
EXECUTE FUNCTION update_work_report_total_hours();

COMMENT ON FUNCTION update_work_report_total_hours() IS 'Automatically recalculates total_hours in work_reports when entries change';

-- =====================================================
-- VIEWS (Optional - for common queries)
-- =====================================================

-- View: Active works summary
CREATE OR REPLACE VIEW v_active_works AS
SELECT
    w.id,
    w.name,
    w.bid_number,
    w.order_number,
    w.order_date,
    w.electrical_schema_progression,
    w.programming_progression,
    w.completed,
    w.invoiced,
    u.first_name || ' ' || u.last_name AS seller_name,
    ac.name AS atix_client_name,
    fc.name AS final_client_name,
    p.name AS plant_name,
    COUNT(DISTINCT wa.user_id) AS assigned_technicians,
    wr.total_hours
FROM works w
LEFT JOIN users u ON w.seller_id = u.id
LEFT JOIN clients ac ON w.atix_client_id = ac.id
LEFT JOIN clients fc ON w.final_client_id = fc.id
LEFT JOIN plants p ON w.plant_id = p.id
LEFT JOIN work_assignments wa ON w.id = wa.work_id
LEFT JOIN work_reports wr ON w.id = wr.work_id
WHERE w.completed = FALSE
GROUP BY w.id, u.first_name, u.last_name, ac.name, fc.name, p.name, wr.total_hours;

COMMENT ON VIEW v_active_works IS 'Summary view of all active (non-completed) works with related information';

-- View: User statistics
CREATE OR REPLACE VIEW v_user_statistics AS
SELECT
    u.id,
    u.first_name || ' ' || u.last_name AS full_name,
    u.email,
    u.user_type,
    u.role,
    COUNT(DISTINCT wa.work_id) AS assigned_works,
    COUNT(DISTINCT CASE WHEN w.completed = FALSE THEN wa.work_id END) AS active_works
FROM users u
LEFT JOIN work_assignments wa ON u.id = wa.user_id
LEFT JOIN works w ON wa.work_id = w.id
GROUP BY u.id, u.first_name, u.last_name, u.email, u.user_type, u.role;

COMMENT ON VIEW v_user_statistics IS 'Statistics for each user including work assignment counts';

-- =====================================================
-- DATABASE INFORMATION
-- =====================================================

COMMENT ON DATABASE postgres IS 'Atix Backend Database - Work Order Management System';

-- =====================================================
-- END OF SCHEMA
-- =====================================================

-- To verify the schema was created successfully, run:
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;
