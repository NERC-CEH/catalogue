drop table if exists metadata, coupledResources;

create table metadata (	
  fileIdentifier varchar(100) not null,  
  resourceIdentifier varchar(100), 
  title varchar(1000),
  parentIdentifier varchar(100),
  revisionOfIdentifier varchar(100),
  primary key (fileIdentifier)
); 

create table coupledResources (
  fileIdentifier varchar(100) not null, 
  resourceIdentifier varchar(100) not null, 
  primary key (fileIdentifier, resourceIdentifier)
);

create index metadata_resourceIdentifier_indx on metadata (resourceIdentifier);
create index metadata_parentIdentifier_indx on metadata (parentIdentifier);
create index metadata_revisionOfIdentifier_indx on metadata (revisionOfIdentifier);
create index coupledResources_resourceIdentifier_indx on coupledResources (resourceIdentifier);
create index coupledResources_fileIdentifier_indx on coupledResources (fileIdentifier);