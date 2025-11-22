using autoescalateOps as db from '../db/schema';

service AutoEscalateService @(path: 'autoescalate') {
  entity Issues      as projection on db.Issues;
  entity Users       as projection on db.Users;
  entity Teams       as projection on db.Teams;
  entity Comments    as projection on db.Comments;
  entity Attachments as projection on db.Attachments;
  entity AuditLogs   as projection on db.AuditLogs;
  entity Machines    as projection on db.Machines;
}
