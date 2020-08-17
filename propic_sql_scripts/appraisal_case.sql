create or replace view "%(db)s"."client_with_appr_v2" as  
with   
	a as (  
	select id as acct_sf_id  
		, name  
		, personmobilephone  
		, personemail  
	from "%(db)s"."%(crm)s_client_contact"  
	where personmobilephone is not null or personemail is not null 
	),  
	bb as (  
	select prpt__buyer_acc__c  
		, prpt__listing_1__c  
		, from_iso8601_timestamp(lastmodifieddate) as bm_record_lmd 
	from "%(db)s"."%(crm)s_prpt__buyer_interest_tbl"  
	where hadoop = 'Vendor' and prpt__listing_type__c = 'Sale'  
	),  
	b as (  
	select prpt__buyer_acc__c  
		, prpt__listing_1__c  
	from (  
		select *  
			, rank() over (partition by prpt__listing_1__c order by bm_record_lmd desc) as rnk  
		from bb  
		)  
	where rnk = 1  
	),  
	cc as (  
	select id as appr_sf_id  
		, cast(substr(redhat, 1, 10) as date) as kafka 
		, ownerid as appraisal_owner  
		, prpt__property__c  
	from "%(db)s"."%(crm)s_prpt__appr_tbl"  
	where redhat is not null and prpt__status__c = 'Appraised'  
	),  
	c as (  
	select *   
	from (  
		select *  
			, rank() over (partition by prpt__property__c, appraisal_owner order by kafka desc) as rnk  
		from cc  
		)  
	where rnk = 1  
	),  
	d as (  
	select *  
	from "%(db)s"."cleaned_prpt_property_view"  
	),  
	e as (  
	select id as user_sf_id  
		, firstname as user_firstname   
		, lastname as user_lastname   
	from "%(db)s"."agent_detail"  
	) 
select a.*, c.*, d.*, e.* 
from a  
inner join b  
on a.acct_sf_id = b.prpt__buyer_acc__c  
inner join c  
on b.prpt__listing_1__c = c.appr_sf_id  
inner join d  
on c.prpt__property__c = d.prop_sf_id  
inner join e  
on c.appraisal_owner = e.user_sf_id###  
 
create or replace view "%(db)s"."np_appraisal_v2" as  
select a.acct_sf_id  
	, a.user_sf_id  
	, a.kafka  
	, b.address_id  
	, 'sce_appr' as had_appraisal_flag  
from "%(db)s"."client_with_appr_v2" as a  
inner join "datalake_dbicd"."cleaned_complete_address_np_view" as b 
on a.final_address = b.full_address 
where a.kafka > b.saledate_final###  
