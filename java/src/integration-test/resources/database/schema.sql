create table metadata (	
  fileIdentifier varchar(50) not null,  
  resourceIdentifier varchar(50), 
  title varchar(1000),
  primary key (fileIdentifier)
); 

create table coupledResources (
  fileIdentifier varchar(50) not null, 
  resourceIdentifier varchar(50) not null, 
  primary key (fileIdentifier, resourceIdentifier)
);

create index metadata_resourceIdentifier_indx on metadata (resourceIdentifier);
create index coupledResources_resourceIdentifier_indx on coupledResources (resourceIdentifier);
create index coupledResources_fileIdentifier_indx on coupledResources (fileIdentifier);