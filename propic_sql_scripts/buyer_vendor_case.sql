create or replace view "%(db)s"."buyer_vendor_v2" as  
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
		, name  
		, personmobilephone  
		, personemail  
	from "%(db)s"."%(crm)s_client_contact"  
	where personmobilephone is not null or personemail is not null  
	),  
	d as ( 
	select prpt__buyer_acc__c  
		, hadoop  
		, prpt__listing_1__c  
		, cast(substr(createddate, 1, 10) as date) as spark  
	from "%(db)s"."%(crm)s_prpt__buyer_interest_tbl"  
	where hadoop in ('snowflake', 'terraform')  
	),  
	e as ( 
	select id as listing_sf_id 
		, ownerid as listing_owner 
	from "%(db)s"."%(crm)s_prpt__appr_tbl" 
	where prpt__sale_or_lease__c = 'Sale' 
	),  
	f as (  
	select id as user_sf_id  
		, firstname as user_firstname   
		, lastname as user_lastname   
	from "%(db)s"."agent_detail"  
	)  
select a.*, c.*, d.*, e.*, f.*  
from a  
inner join b  
on a.prop_sf_id = b.prpt__Property__c  
inner join c  
on b.prpt__Client__c = c.acct_sf_id  
inner join d  
on c.acct_sf_id = d.prpt__buyer_acc__c  
inner join e  
on d.prpt__listing_1__c = e.listing_sf_id 
inner join f  
on e.listing_owner = f.user_sf_id  
where spark between (current_date - INTERVAL '180' DAY) and current_date and  
	a.final_address not in ( 
			select full_address  
			from "datalake_dbicd"."cleaned_complete_address_np_view"  
			where listingsource = 'BUY' and status = 'current')###  
 
create or replace view "%(db)s"."np_buyer_vendor_v2" as  
select a.acct_sf_id  
	, a.user_sf_id  
	, a.spark  
	, b.address_id  
	, 'sce_bv' as buyer_vendor_flag  
from ( 
	select final_address, acct_sf_id, user_sf_id, spark 
		, rank() over (partition by acct_sf_id, user_sf_id order by spark desc) as rnk 
	from "%(db)s"."buyer_vendor_v2" 
) as a 
inner join "datalake_dbicd"."cleaned_complete_address_np_view" as b 
on a.final_address = b.full_address 
where a.rnk = 1###  