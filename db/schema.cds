namespace autoescalateOps;

entity Issues {
    key ID              : UUID;
    title               : String(100);
    description         : String(1000);
    type                : String(50);      // e.g. MACHINE_BREAKDOWN, QUALITY_DEFECT
    priority            : String(20);      // e.g. Low, Medium, High, Critical
    status              : String(20);      // e.g. New, InProgress, Resolved, Closed
    severity            : Integer;         // numeric 1–5 if needed

    location            : String(100);     // plant / area / line
    reportedAt          : DateTime;
    acknowledgedAt      : DateTime;
    resolvedAt          : DateTime;
    dueAt               : DateTime;
    slaBreached         : Boolean;
    lastUpdatedAt       : DateTime;

    reporter            : Association to Users;
    assignedTeam        : Association to Teams;
    assignedUser        : Association to Users;
    machine             : Association to Machines;
}

entity Users {
    key ID      : UUID;
    name        : String(100);
    email       : String(255);
    role        : String(30);      // OPERATOR, SUPERVISOR, MANAGER
    isActive    : Boolean;
    createdAt   : DateTime;

    team        : Association to Teams;
}

entity Teams {
    key ID              : UUID;
    name                : String(100);
    description         : String(255);
    defaultIssueTypes   : String(255);     // e.g. "MACHINE_BREAKDOWN, MAINTENANCE"
    createdAt           : DateTime;

    members             : Association to many Users on members.team = $self;
    issues              : Association to many Issues on issues.assignedTeam = $self;
}

entity Comments {
    key ID      : UUID;
    message     : String(1000);
    createdAt   : DateTime;

    issue       : Association to Issues;
    author      : Association to Users;
}

entity Attachments {
    key ID      : UUID;
    url         : String(500);      // file URL or path
    fileName    : String(255);
    fileType    : String(100);      // e.g. image/jpeg
    createdAt   : DateTime;

    issue       : Association to Issues;
    uploadedBy  : Association to Users;
}

entity AuditLogs {
    key ID      : UUID;
    eventType   : String(50);       // STATUS_CHANGED, ASSIGNED, COMMENT_ADDED, SLA_BREACHED
    oldValue    : String(255);
    newValue    : String(255);
    notes       : String(1000);
    createdAt   : DateTime;

    issue       : Association to Issues;
    actor       : Association to Users;
}

entity Machines {
    key ID      : UUID;
    name        : String(100);
    location    : String(100);
    assetTag    : String(100);
    isActive    : Boolean;
    createdAt   : DateTime;

    issues      : Association to many Issues on issues.machine = $self;
}
