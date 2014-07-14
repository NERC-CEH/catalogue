drop table metadata if exists;
drop table coupledResources if exists;

create table metadata (	
  fileIdentifier varchar(100) not null,  
  resourceIdentifier varchar(100), 
  title varchar(1000),
  primary key (fileIdentifier)
); 

create table coupledResources (
  fileIdentifier varchar(100) not null, 
  resourceIdentifier varchar(100) not null, 
  primary key (fileIdentifier, resourceIdentifier)
);

create index metadata_resourceIdentifier_indx on metadata (resourceIdentifier);
create index coupledResources_resourceIdentifier_indx on coupledResources (resourceIdentifier);
create index coupledResources_fileIdentifier_indx on coupledResources (fileIdentifier);