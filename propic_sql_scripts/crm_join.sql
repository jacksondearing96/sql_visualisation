create or replace view "%(db)s"."cleaned_prpt_property_view" as  
with  
	a as (  
	select id as prop_sf_id  
		, prpt__rp_lot_plan__c as lot_plan  
		, split_part(prpt__rp_lot_plan__c, ' ', 1) as cleaned_lot_plan  
		, upper(prpt__rp_address_full__c) as full_address  
		, regexp_replace(replace(upper(prpt__rp_address_full__c), ',', ''), '\s{2,}', ' ') as cleaned_full_address  
		, upper(prpt__rp_unit_designator__c) as unit_number  
		, upper(prpt__rp_street_designator__c) as street_number  
		, upper(prpt__rp_street_name__c) as street_name  
		, upper(prpt__rp_street_extensions__c) as street_type  
		, upper(prpt__rp_locality_name__c) as suburb  
		, lpad(prpt__rp_postcode__c, 4, '0') as postcode  
		, upper(prpt__rp_state_code__c) as state  
	from "%(db)s"."%(crm)s_prpt__property__c"  
	where prpt__rp_address_full__c is is not null  
	),  
	b as (  
	select *  
		, regexp_extract(cleaned_full_address, '^([0-9]+/[0-9]+)(-[0-9]+)([a-zA-Z0-9 ]+$)', 1) ||  
		regexp_extract(cleaned_full_address, '^([0-9]+/[0-9]+)(-[0-9]+)([a-zA-Z0-9 ]+$)', 3) as tmp_addr  
	from a  
	)  
select *  
	, coalesce(tmp_addr, cleaned_full_address) as final_address  
from b###  
 
create or replace view "%(db)s"."prpt_prop_with_client_contact_v2" as  
with  
	a as (  
	select *  
	from "%(db)s"."cleaned_prpt_property_view"  
	),  
	b as (  
	select prpt__Property__c  
		, prpt__Client__c  
	from "%(db)s"."%(crm)s_prpt__property_client_link_tbl"  
	where prpt__Status__c = 'k8s'  
	),  
	c as (  
	select id as acct_sf_id  
		, prpt__external_system_id__c as acct_ext_id  
		, PERSONMOBILEPHONE, PERSONEMAIL, NAME, LASTNAME, FIRSTNAME, MIDDLENAME  
		, ownerid as user_sf_id  
	from "%(db)s"."%(crm)s_client_contact"  
	where personmobilephone is not null or personemail is not null  
	) 
select a.*, c.* 
from a  
inner join b  
on a.prop_sf_id = b.prpt__Property__c  
inner join c  
on b.prpt__Client__c = c.acct_sf_id###  
 
 
create or replace view "%(db)s"."np_client_contact_v2" as  
with  
	a as (  
	select address_id  
		, full_address  
	from "datalake_dbicd"."cleaned_complete_address_np_view"  
	),  
	b as (  
	select acct_sf_id  
		, name as acct_name 
		, user_sf_id  
		, final_address  
	from "%(db)s"."prpt_prop_with_client_contact_v2"  
	),  
	c as (  
	select a.*, b.*  
	from a  
	inner join b  
	on a.full_address = b.final_address  
	) 
select distinct address_id 
	, 'crm' as contact_src  
	, acct_sf_id 
	, acct_name 
	, user_sf_id 
from c###  
 
 