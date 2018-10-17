package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.grail.kantar.beans.KantarReportTypeBean;
import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.MetaFieldMapping;
import com.grail.synchro.dao.SynchroMetaFieldDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.database.dao.DAOException;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SynchroMetaFieldDAOImpl extends SynchroAbstractDAO implements SynchroMetaFieldDAO {
    private static final Logger LOG = Logger.getLogger(SynchroMetaFieldDAOImpl.class);
    private static final String LOAD_ENDMARKET_FIELDS = "SELECT id, name FROM grailEndMarketFields WHERE isActive = 1 order by sortorder asc";
    
    private static final String LOAD_ALL_ENDMARKET_FIELDS = "SELECT id, name FROM grailEndMarketFields order by sortorder asc";
    
    private static final String INSERT_ENDMARKET_FIELD = "INSERT INTO grailEndMarketFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_ENDMARKET_FIELD = "UPDATE grailEndMarketFields SET name=? where id = ?";
    private static final String DELETE_ENDMARKET_FIELD = "UPDATE grailEndMarketFields SET isActive = 0 where id = ?";
    private static final String UPDATE_ENDMARKET_SORTFIELD = "UPDATE grailEndMarketFields SET sortOrder = ? where id = ?";
    private static final String LOAD_ENDMARKET_FIELDS_BY_REGION = "SELECT endmarket_t.id, endmarket_t.name from grailendmarketfields as endmarket_t join grailRegionFieldMappingFields as mapping_t"+
            " ON endmarket_t.id=mapping_t.endmarketid and mapping_t.regionid = ? where endmarket_t.isActive = 1 order by endmarket_t.sortorder asc";
    private static final String LOAD_ENDMARKET_FIELDS_BY_AREA = "SELECT endmarket_t.id, endmarket_t.name from grailendmarketfields as endmarket_t join grailAreaFieldMappingFields as mapping_t"+
            " ON endmarket_t.id=mapping_t.endmarketid and mapping_t.areaid = ? where endmarket_t.isActive = 1 order by endmarket_t.sortorder asc";


	 private static final String LOAD_METHODOLOGY_FIELDS = "SELECT id, name, islessfrequent, briefexception, proposalexception, agencywaiverexception, repsummaryexception, brandspecific FROM grailMethodologyFields WHERE isActive = 1 order by sortorder asc";
    private static final String LOAD_ALL_METHODOLOGY_FIELDS = "SELECT id, name, islessfrequent, briefexception, proposalexception, agencywaiverexception, repsummaryexception, brandspecific FROM grailMethodologyFields order by sortorder asc";
    private static final String INSERT_METHODOLOGY_FIELD = "INSERT INTO grailMethodologyFields (id,name,islessfrequent, briefexception, proposalexception, agencywaiverexception, repsummaryexception, brandspecific, sortOrder) VALUES(?,?,?,?,?,?,?,?, ?)";
    private static final String UPDATE_METHODOLOGY_FIELD = "UPDATE grailMethodologyFields SET name=?, islessfrequent=?, briefexception=?, proposalexception=?, agencywaiverexception=?, repsummaryexception=?, brandspecific=? where id = ?";
    private static final String INSERT_METHDOLOGY_MAPPING_FIELD = "INSERT INTO grailMethFieldMappingFields(methodologyid, methodologygroupid) VALUES(?, ?)";
    private static final String DELETE_METHDOLOGY_FIELD = "UPDATE grailMethodologyFields SET isActive = 0 where id = ?";
    private static final String UPDATE_METHDOLOGY_SORTFIELD = "UPDATE grailMethodologyFields SET sortOrder = ? where id = ?";
    private static final String DELETE_MAPPING_BY_METHODOLOGY = "DELETE FROM grailMethFieldMappingFields where methodologyid = ?";
    private static final String DELETE_MAPPING_BY_METHODOLOGYGROUP = "DELETE FROM grailMethFieldMappingFields where methodologygroupid = ?";

    private static final String LOAD_METHODOLOGY_GROUP_FIELDS = "SELECT id, name FROM grailMethodologyGroupFields WHERE isActive = 1 order by sortorder asc";
    private static final String LOAD_METHODOLOGY__FIELDS_BY_GROUP = "SELECT methodology_t.id, methodology_t.name, methodology_t.islessfrequent, methodology_t.briefexception, methodology_t.proposalexception, methodology_t.agencywaiverexception,"
    		+ "  methodology_t.repsummaryexception from grailMethodologyFields as methodology_t join grailMethFieldMappingFields as mapping_t "+
            "ON methodology_t.id=mapping_t.methodologyid and mapping_t.methodologygroupid = ? where methodology_t.isActive = 1 order by methodology_t.sortorder asc";
    
    private static final String LOAD_ALL_METHODOLOGY__FIELDS_BY_GROUP = "SELECT methodology_t.id, methodology_t.name, methodology_t.islessfrequent, methodology_t.briefexception, methodology_t.proposalexception, methodology_t.agencywaiverexception,"
    		+ "  methodology_t.repsummaryexception from grailMethodologyFields as methodology_t join grailMethFieldMappingFields as mapping_t "+
            "ON methodology_t.id=mapping_t.methodologyid and mapping_t.methodologygroupid = ? order by methodology_t.sortorder asc";
    
    private static final String INSERT_METHODOLOGY_GROUP_FIELD = "INSERT INTO grailMethodologyGroupFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_METHODOLOGY_GROUP_FIELD = "UPDATE grailMethodologyGroupFields SET name=? where id = ?";
    private static final String DELETE_METHODOLOGY_GROUP_FIELD = "UPDATE grailMethodologyGroupFields SET isActive = 0 where id = ?";
    private static final String UPDATE_METHODOLOGY_GROUP_SORTFIELD = "UPDATE grailMethodologyGroupFields SET sortOrder = ? where id = ?";

    private static final String LOAD_PRODUCT_FIELDS = "SELECT id, name FROM grailProductFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_PRODUCT_FIELD = "INSERT INTO grailProductFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_PRODUCT_FIELD = "UPDATE grailProductFields SET name=? where id = ?";
    private static final String DELETE_PRODUCT_FIELD = "UPDATE grailProductFields SET isActive = 0 where id = ?";
    private static final String UPDATE_PRODUCT_SORTFIELD = "UPDATE grailProductFields SET sortOrder = ? where id = ?";

    private static final String LOAD_BRAND_FIELDS = "SELECT id, name, brandtype FROM grailBrandFields WHERE isActive = 1 order by sortorder asc";
    
    private static final String LOAD_ALL_BRAND_FIELDS = "SELECT id, name, brandtype FROM grailBrandFields order by sortorder asc";
    
    private static final String GET_BRAND_ID_BY_NAME = "SELECT id FROM grailBrandFields WHERE name = ?";
    private static final String LOAD_BRAND_FIELDS_BY_PRODUCT = "SELECT brand_t.id, brand_t.name from grailBrandFields as brand_t join grailPBFieldMappingFields as mapping_t "+
            "ON brand_t.id=mapping_t.brandid and mapping_t.productid = ? WHERE brand_t.isActive = 1 order by brand_t.sortorder asc";
    private static final String INSERT_BRAND_FIELD = "INSERT INTO grailBrandFields(id,name,sortOrder) VALUES(?,?,?)";
    private static final String INSERT_BRAND_MAPPING_FIELD = "INSERT INTO grailPBFieldMappingFields(productid,brandid) VALUES(?,?)";
    private static final String DELETE_BRAND_FIELD = "UPDATE grailBrandFields SET isActive = 0 where id = ?";
    private static final String UPDATE_BRAND_SORTFIELD = "UPDATE grailBrandFields SET sortOrder = ? where id = ?";
    private static final String DELETE_MAPPING_BY_PRODUCT = "DELETE FROM grailPBFieldMappingFields where productid = ?";
    private static final String DELETE_MAPPING_BY_BRAND = "DELETE FROM grailPBFieldMappingFields where brandid = ?";

    private static final String LOAD_SUPPLIER_GROUP_FIELDS = "SELECT id, name FROM grailSupplierGroupFields";
    private static final String INSERT_SUPPLIER_GROUP_FIELD = "INSERT INTO grailSupplierGroupFields (id,name) VALUES(?,?)";
    private static final String INSERT_SUPPLIER_MAPPING_FIELD = "INSERT INTO grailSupplierGrpFieldMapping(suppliergroupid, supplierid) VALUES(?, ?)";
    private static final String UPDATE_SUPPLIER_GROUP_FIELD = "UPDATE grailSupplierGroupFields SET name=? where id = ?";

    private static final String LOAD_SUPPLIER_FIELDS = "SELECT id, name FROM grailSupplierFields";
    private static final String LOAD_SUPPLIER_BY_GROUP = "SELECT supplier_t.id, supplier_t.name from grailSupplierFields as supplier_t join grailSupplierGrpFieldMapping as mapping_t"+
            "																					ON supplier_t.id=mapping_t.supplierid and mapping_t.suppliergroupid = ?";
    private static final String INSERT_SUPPLIER_FIELD = "INSERT INTO grailSupplierFields (id,name) VALUES(?,?)";
    private static final String UPDATE_SUPPLIER_FIELD = "UPDATE grailSupplierFields SET name=? where id = ?";

    private static final String LOAD_FWSUPPLIER_GROUP_FIELDS = "SELECT id, name FROM grailFwSupplierGFields";
    private static final String INSERT_FWSUPPLIER_GROUP_FIELD = "INSERT INTO grailFwSupplierGFields (id,name) VALUES(?,?)";
    private static final String INSERT_FWSUPPLIER_MAPPING_FIELD = "INSERT INTO grailFwSupplierGFieldMapping(fwsuppliergroupid, fwsupplierid) VALUES(?, ?)";
    private static final String UPDATE_FWSUPPLIER_GROUP_FIELD = "UPDATE grailFwSupplierGFields SET name = ? where id = ?";

    private static final String LOAD_FWSUPPLIER_FIELDS = "SELECT id, name FROM grailFwSupplierFields";
    private static final String LOAD_FWSUPPLIER_BY_GROUP = "SELECT supplier_t.id, supplier_t.name from grailFwSupplierFields as supplier_t join grailFwSupplierGFieldMapping as mapping_t"+
            "																					ON supplier_t.id=mapping_t.fwsupplierid and mapping_t.fwsuppliergroupid = ?";
    private static final String INSERT_FWSUPPLIER_FIELD = "INSERT INTO grailFwSupplierFields (id,name) VALUES(?,?)";
    private static final String UPDATE_FWSUPPLIER_FIELD = "UPDATE grailFwSupplierFields SET name=? where id = ?";

    private static final String UPDATE_BRAND_FIELD = "UPDATE grailBrandFields SET name = ? where id = ?";

    private static final String PRODUCTS_BY_BRAND = "SELECT id, name FROM grailpbfieldmappingfields AS mapping_t JOIN grailproductfields as product_t ON product_t.id=mapping_t.productid where mapping_t.brandid = ?";

    private static final String LOAD_CURRENCY_FIELDS = "SELECT id, name, description, isglobal FROM grailCurrencyFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_CURRENCY_FIELD = "INSERT INTO grailCurrencyFields (id, name, description, isglobal, sortOrder) VALUES(?,?,?,?,?)";
    private static final String UPDATE_CURRENCY_FIELD = "UPDATE grailCurrencyFields SET name=?, description=?, isglobal=? where id = ?";
    private static final String DELETE_CURRENCY_FIELD = "UPDATE grailCurrencyFields SET isActive = 0 where id = ?";
    private static final String UPDATE_CURRENCY_SORTFIELD = "UPDATE grailCurrencyFields SET sortOrder = ? where id = ?";

    private static final String LOAD_EXCHANGE_RATES = "SELECT currencyid, exchangerate FROM grailcurrencyexchangerate";
    private static final String LOAD_EXCHANGE_RATE_BY_ID = "SELECT currencyid, exchangerate FROM grailcurrencyexchangerate WHERE currencyid = ? AND modificationdate = (select max(er.modificationdate) from grailcurrencyexchangerate er where er.currencyid = ?)";

    private static final String LOAD_TAGENCY_FIELDS = "SELECT id, name FROM grailTAgencyFields";
    private static final String INSERT_TAGENCY_FIELD = "INSERT INTO grailTAgencyFields (id, name) VALUES(?,?)";
    private static final String UPDATE_TAGENCY_FIELD = "UPDATE grailTAgencyFields SET name=? where id = ?";

    private static final String LOAD_DCOLLECTION_FIELDS = "SELECT id, name FROM grailDataCollectionFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_DCOLLECTION_FIELD = "INSERT INTO grailDataCollectionFields (id, name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_DCOLLECTION_FIELD = "UPDATE grailDataCollectionFields SET name=? where id = ?";
    private static final String DELETE_DCOLLECTION_FIELD = "UPDATE grailDataCollectionFields SET isActive = 0 where id = ?";
    private static final String UPDATE_DCOLLECTION_SORTFIELD = "UPDATE grailDataCollectionFields SET sortOrder = ? where id = ?";

    private static final String LOAD_MTYPE_FIELDS = "SELECT id, name FROM grailMethodologyTypeFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_MTYPE_FIELD = "INSERT INTO grailMethodologyTypeFields (id, name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_MTYPE_FIELD = "UPDATE grailMethodologyTypeFields SET name=? where id = ?";
    private static final String DELETE_MTYPE_FIELD = "UPDATE grailMethodologyTypeFields SET isActive = 0 where id = ?";
    private static final String UPDATE_MTYPE_SORTFIELD = "UPDATE grailMethodologyTypeFields SET sortOrder = ? where id = ?";

    private static final String INSERT_COLLECTION_MAPPING_FIELD = "INSERT INTO grailMethCollectionMapping(methodologyid, collectionid) VALUES(?, ?)";
    private static final String DELETE_MAPPING_BY_METHODOLOGYTYPE = "DELETE FROM grailMethCollectionMapping where methodologyid = ?";
    private static final String DELETE_MAPPING_BY_COLLECTION = "DELETE FROM grailMethCollectionMapping where collectionid = ?";

    private static final String INSERT_METHODOLOGY_TYPE_MAPPING_FIELD = "INSERT INTO grailMethTypeMappingFields(methodologytypeid, methodologyid) VALUES(?, ?)";
    private static final String DELETE_METHODOLOGY_TYPE_MAPPING_BY_TYPE = "DELETE FROM grailMethTypeMappingFields where methodologytypeid = ?";
    private static final String DELETE_METHODOLOGY_TYPE_MAPPING_BY_METHODOLOGY = "DELETE FROM grailMethTypeMappingFields where methodologyid = ?";

    private static final String LOAD_METHODOLOGY_MAPPING_BY_TYPE = "SELECT methodology_t.id, methodology_t.name, methodology_t.islessfrequent, methodology_t.briefexception, methodology_t.proposalexception, methodology_t.agencywaiverexception, methodology_t.repsummaryexception, methodology_t.brandspecific from grailMethodologyFields as methodology_t join grailMethTypeMappingFields as mapping_t "+
            "ON methodology_t.id=mapping_t.methodologyid and mapping_t.methodologytypeid = ? where methodology_t.isActive = 1 order by methodology_t.sortorder asc";
    private static final String LOAD_UNSELECTED_METHODOLOGY_MAPPING_BY_TYPE = "SELECT m.id, m.name from grailMethodologyFields m where m.id not in (select mp.methodologyid from grailMethTypeMappingFields mp group by mp.methodologyid)";
    private static final String GET_METH_TYPE_BY_PROP_METHODOLOGY = "SELECT mp.methodologytypeid from grailMethTypeMappingFields mp where mp.methodologyid = ? group by mp.methodologytypeid";


    private static final String INSERT_METHODOLOGY_GROUP_TYPE_MAPPING_FIELD = "INSERT INTO grailMethGroupTypeMapping(methodologytypeid, methodologygroupid) VALUES(?, ?)";
    private static final String DELETE_METHODOLOGY_GROUP_TYPE_MAPPING_BY_TYPE = "DELETE FROM grailMethGroupTypeMapping where methodologytypeid = ?";
    private static final String DELETE_METHODOLOGY_GROUP_TYPE_MAPPING_BY_GROUP = "DELETE FROM grailMethGroupTypeMapping where methodologygroupid = ?";
    private static final String LOAD_UNSELECTED_METHODOLOGY_GROUP_MAPPING_BY_TYPE = "SELECT m.id, m.name from grailMethodologyGroupFields m where m.id not in (select mp.methodologygroupid from grailMethGroupTypeMapping mp group by mp.methodologygroupid)";
    private static final String LOAD_METHODOLOGY_GROUP_MAPPING_BY_TYPE = "SELECT methodology_t.id, methodology_t.name from grailMethodologyGroupFields as methodology_t join grailMethGroupTypeMapping as mapping_t "+
            "ON methodology_t.id=mapping_t.methodologygroupid and mapping_t.methodologytypeid = ? where methodology_t.isActive = 1 order by methodology_t.sortorder asc";
    private static final String GET_METH_TYPE_BY_GROUP = "SELECT mp.methodologytypeid from grailMethGroupTypeMapping mp where mp.methodologygroupid = ? group by mp.methodologytypeid";




    private static final String LOAD_DCOLLECTION_FIELDS_BY_TYPE = "SELECT id, name FROM grailMethCollectionMapping AS mapping_t JOIN grailDataCollectionFields as collection_t " +
            "ON collection_t.id=mapping_t.collectionid where mapping_t.methodologyid = ? AND collection_t.isActive = 1 order by collection_t.sortorder asc";

    private static final String LOAD_REGION_FIELDS = "SELECT id, name FROM grailRegionFields WHERE isActive = 1 order by sortorder asc";
    private static final String LOAD_ALL_REGION_FIELDS = "SELECT id, name FROM grailRegionFields order by sortorder asc";
    
    private static final String INSERT_REGION_FIELD = "INSERT INTO grailRegionFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_REGION_FIELD = "UPDATE grailRegionFields SET name=? where id = ?";
    private static final String DELETE_REGION_FIELD = "UPDATE grailRegionFields SET isActive = 0 where id = ?";
    private static final String UPDATE_REGION_SORTFIELD = "UPDATE grailRegionFields SET sortOrder = ? where id = ?";
    private static final String INSERT_REGION_MAPPING_FIELD = "INSERT INTO grailRegionFieldMappingFields(regionid,endmarketid) VALUES(?,?)";
    private static final String DELETE_MAPPING_BY_REGION = "DELETE FROM grailRegionFieldMappingFields where regionid = ?";
    private static final String DELETE_MAPPING_BY_MARKET = "DELETE FROM grailRegionFieldMappingFields where endmarketid = ?";


    private static final String LOAD_AREA_FIELDS = "SELECT id, name FROM grailAreaFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_AREA_FIELD = "INSERT INTO grailAreaFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_AREA_FIELD = "UPDATE grailAreaFields SET name=? where id = ?";
    private static final String DELETE_AREA_FIELD = "UPDATE grailAreaFields SET isActive = 0 where id = ?";
    private static final String UPDATE_AREA_SORTFIELD = "UPDATE grailAreaFields SET sortOrder = ? where id = ?";
    private static final String INSERT_AREA_MAPPING_FIELD = "INSERT INTO grailAreaFieldMappingFields(areaid,endmarketid) VALUES(?,?)";
    private static final String DELETE_MAPPING_BY_AREA = "DELETE FROM grailAreaFieldMappingFields where areaid = ?";

    private static final String LOAD_JOBTITLE_FIELDS = "SELECT id, name FROM grailJobTitleFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_JOBTITLE_FIELD = "INSERT INTO grailJobTitleFields (id,name,sortOrder) VALUES(?,?,?)";
    private static final String UPDATE_JOBTITLE_FIELD = "UPDATE grailJobTitleFields SET name=? where id = ?";
    private static final String DELETE_JOBTITLE_FIELD = "UPDATE grailJobTitleFields SET isActive = 0 where id = ?";
    private static final String UPDATE_JOBTITLE_SORTFIELD = "UPDATE grailJobTitleFields SET sortOrder = ? where id = ?";

    private static final String DELETE_CURRENCY_MAPPING_BY_COUNTRYID = "DELETE FROM grailCountryCurrency where endmarketid = ?";
    private static final String INSERT_COUNTRY_CURRENCY_MAPPING = "INSERT INTO grailCountryCurrency(endmarketid, currencyid) VALUES(?,?)";

    private static final String INSERT_KANTAR_REPORT_TYPE = "INSERT INTO grailkantarreporttype (id, name, isActive,otherType,sortorder) VALUES (?,?,?,?,?)";
    private static final String UPDATE_KANTAR_REPORT_TYPE = "UPDATE grailkantarreporttype set name=? where id=?";
    private static final String DELETE_KANTAR_REPORT_TYPE = "UPDATE grailkantarreporttype set isactive=0 where id=?";
    private static final String LOAD_KANTAR_REPORT_TYPES = "SELECT id, name FROM grailkantarreporttype WHERE isActive = 1 order by sortorder";
    private static final String LOAD_KANTAR_REPORT_TYPE_BEANS = "SELECT id, name, isActive,otherType,sortorder FROM grailkantarreporttype WHERE isActive = 1 order by sortorder";
    private static final String LOAD_KANTAR_REPORT_TYPE_BY_NAME = "SELECT id, name FROM grailkantarreporttype WHERE isActive=1 AND lower(name) = ?";
    private static final String LOAD_KANTAR_REPORT_TYPE_BY_ID = "SELECT id, name FROM grailkantarreporttype WHERE id = ?";
    private static final String UPDATE_KANTAR_REPORT_TYPE_SORTFIELD = "UPDATE grailkantarreporttype SET sortOrder = ? where id = ?";


    private static final String INSERT_KANTAR_BUTTON_METHODOLOGY_TYPE = "INSERT INTO grailkantarbtnmethodologytype (id, name, sortorder, isActive) VALUES (?,?,?,?)";
    private static final String LOAD_KANTAR_BUTTON_METHODOLOGY_TYPES = "SELECT id, name FROM grailkantarbtnmethodologytype WHERE isActive = 1 order by sortorder";
    private static final String LOAD_ALL_KANTAR_BUTTON_METHODOLOGY_TYPES = "SELECT id, name FROM grailkantarbtnmethodologytype order by sortorder";
    private static final String LOAD_KANTAR_BUTTON_METHODOLOGY_TYPE_BY_NAME = "SELECT id, name FROM grailkantarbtnmethodologytype WHERE lower(name) = ?";
    private static final String LOAD_KANTAR_BUTTON_METHODOLOGY_TYPE_BY_ID = "SELECT id, name FROM grailkantarbtnmethodologytype WHERE id = ?";
    private static final String UPDATE_KANTAR_BUTTON_METHODOLOGY_TYPE = "UPDATE grailkantarbtnmethodologytype SET name=? where id = ?";
    private static final String DELETE_KANTAR_BUTTON_METHODOLOGY_TYPE = "UPDATE grailkantarbtnmethodologytype SET isActive = 0 where id = ?";
    private static final String UPDATE_KANTAR_BUTTON_METHODOLOGY_TYPE_SORTFIELD = "UPDATE grailkantarbtnmethodologytype SET sortOrder = ? where id = ?";

    private static final String INSERT_GRAIL_BUTTON_METHODOLOGY_TYPE = "INSERT INTO grailbuttonmethodologytype (id, name, sortorder, isActive) VALUES (?,?,?,?)";
    private static final String LOAD_GRAIL_BUTTON_METHODOLOGY_TYPES = "SELECT id, name FROM grailbuttonmethodologytype WHERE isActive = 1 order by sortorder";
    private static final String LOAD_ALL_GRAIL_BUTTON_METHODOLOGY_TYPES = "SELECT id, name FROM grailbuttonmethodologytype order by sortorder";
    private static final String LOAD_GRAIL_BUTTON_METHODOLOGY_TYPE_BY_NAME = "SELECT id, name FROM grailbuttonmethodologytype WHERE lower(name) = ?";
    private static final String LOAD_GRAIL_BUTTON_METHODOLOGY_TYPE_BY_ID = "SELECT id, name FROM grailbuttonmethodologytype WHERE id = ?";
    private static final String UPDATE_GRAIL_BUTTON_METHODOLOGY_TYPE = "UPDATE grailbuttonmethodologytype SET name=? where id = ?";
    private static final String DELETE_GRAIL_BUTTON_METHODOLOGY_TYPE = "UPDATE grailbuttonmethodologytype SET isActive = 0 where id = ?";
    private static final String UPDATE_GRAIL_BUTTON_METHODOLOGY_TYPE_SORTFIELD = "UPDATE grailbuttonmethodologytype SET sortOrder = ? where id = ?";
    private static final String LOAD_RESEACRCH_AGECNY_FIELDS = "SELECT id, name FROM grailresearchagency WHERE isActive = 1 order by sortorder asc";
    private static final String LOAD_ALL_RESEACRCH_AGECNY_FIELDS = "SELECT id, name FROM grailresearchagency order by sortorder asc";
    
    private static final String LOAD_RESEACRCH_AGECNY_GROUP_FIELDS = "SELECT id, name FROM grailresearchagencygroup WHERE isActive = 1 order by sortorder asc";
    
    private static final String LOAD_RESEARCH_AGENCY_BY_GROUP = "SELECT researchagency_t.id, researchagency_t.name from grailresearchagency as researchagency_t join grailresearchagencymapping as mapping_t "+
            "ON researchagency_t.id=mapping_t.researchagencyid and mapping_t.researchagencygroupid = ? where researchagency_t.isActive = 1 order by researchagency_t.sortorder asc";
    
    private static final String LOAD_ALL_RESEARCH_AGENCY_BY_GROUP = "SELECT researchagency_t.id, researchagency_t.name from grailresearchagency as researchagency_t join grailresearchagencymapping as mapping_t "+
            "ON researchagency_t.id=mapping_t.researchagencyid and mapping_t.researchagencygroupid = ?  order by researchagency_t.sortorder asc";

    private static final String LOAD_GLOBAL_CURRENCY_FIELDS = "SELECT id, name, description, isglobal FROM grailCurrencyFields WHERE isActive = 1 and isglobal =1 order by sortorder asc";
    private static final String LOAD_NON_GLOBAL_CURRENCY_FIELDS = "SELECT id, name, description, isglobal FROM grailCurrencyFields WHERE isActive = 1  and isglobal =0 order by sortorder asc";

    /**
     * Synchro Phase 5
     * @author kanwardeep.grewal
     */
    private static final String LOAD_ENDMARKET_FIELDS_BY_T20T40= "SELECT endmarket_t.id, endmarket_t.name from grailendmarketfields as endmarket_t join grailT20T40MappingMetaFields as mapping_t"+
            " ON endmarket_t.id=mapping_t.eid and mapping_t.tid = ? where endmarket_t.isActive = 1 order by endmarket_t.sortorder asc";
    
    private static final String LOAD_T20T40_FIELDS = "SELECT tid, name FROM grailT20T40MetaFields WHERE isActive = 1 order by sortorder asc";
    private static final String INSERT_T20T40_FIELD = "INSERT INTO grailT20T40MetaFields (tid, name, sortOrder) VALUES(?, ?, ?)";
    private static final String UPDATE_T20T40_FIELD = "UPDATE grailT20T40MetaFields SET name = ? where tid = ?";
    private static final String DELETE_T20T40_FIELD = "UPDATE grailT20T40MetaFields SET isActive = 0 where tid = ?";
    private static final String UPDATE_T20T40_SORTFIELD = "UPDATE grailT20T40MetaFields SET sortOrder = ? where tid = ?";
    private static final String INSERT_T20T40_MAPPING_FIELD = "INSERT INTO grailT20T40MappingMetaFields(tid, eid) VALUES(?,?)";
    private static final String DELETE_MAPPING_BY_T20T40 = "DELETE FROM grailT20T40MappingMetaFields where tid = ?";
    
    private static final String UPDATE_ENDMARKET_OTHER_FIELDS = "UPDATE grailemothersmapping SET approval=?, eu=?, t20_t40=?   where eid = ?";
    
    private static final String INSERT_ENDMARKET_OTHER_FIELD = "INSERT INTO grailemothersmapping (eid, approval, eu, t20_t40) VALUES(?,?,?, ?)";
    
    private static final String GET_APPROVAL_TYPE_BY_EM	 = "SELECT approval FROM grailemothersmapping where eid = ?";
    private static final String GET_MARKET_TYPE_BY_EM	 = "SELECT eu FROM grailemothersmapping where eid = ?";
    
    private static final String GET_T20_T40_TYPE_BY_EM	 = "SELECT t20_t40 FROM grailemothersmapping where eid = ?";
    
    private static final String LOAD_GRAIL_RESEARCH_AGENCY_GROUP_FIELDS = "SELECT id, name FROM grailResearchAgencyGroup";
    private static final String INSERT_GRAIL_RESEARCH_AGENCY_GROUP_FIELD = "INSERT INTO grailResearchAgencyGroup (id,name) VALUES(?,?)";
    private static final String INSERT_GRAIL_RESEARCH_AGENCY_MAPPING_FIELD = "INSERT INTO grailResearchAgencyMapping(researchagencyid, researchagencygroupid) VALUES(?, ?)";
    private static final String UPDATE_GRAIL_RESEARCH_AGENCY_GROUP_FIELD = "UPDATE grailResearchAgencyGroup SET name=? where id = ?";

    private static final String LOAD_GRAIL_RESEARCH_AGENCY_FIELDS = "SELECT id, name FROM grailResearchAgency";
    private static final String LOAD_GRAIL_RESEARCH_AGENCY_BY_GROUP = "SELECT agency_t.id, agency_t.name from grailResearchAgency as agency_t join grailResearchAgencyMapping as mapping_t"+
            "																					ON agency_t.id=mapping_t.researchagencyid and mapping_t.researchagencygroupid = ?";
    private static final String INSERT_GRAIL_RESEARCH_AGENCY_FIELD = "INSERT INTO grailResearchAgency (id,name) VALUES(?,?)";
    private static final String UPDATE_GRAIL_RESEARCH_AGENCY_FIELD = "UPDATE grailResearchAgency SET name=? where id = ?";
    private static final String DELETE_MAPPING_BY_RESEARCH_AGENCY_GROUP = "DELETE FROM grailResearchAgencyMapping where researchagencygroupid = ?";
    private static final String INSERT_MAPPING_BY_RESEARCH_AGENCY_GROUP = "INSERT INTO grailResearchAgencyMapping(researchagencyid, researchagencygroupid) VALUES(?, ?)";
    private static final String DELETE_MAPPING_BY_RESEARCH_AGENCY = "DELETE FROM grailResearchAgencyMapping where researchagencyid = ?";
    
    private static final String UPDATE_RESEARCH_AGENCY_SORTFIELD = "UPDATE grailresearchagency SET sortOrder = ? where id = ?";
    private static final String DELETE_RESEARCH_AGENCY_FIELD = "UPDATE grailresearchagency SET isActive = 0 where id = ?";
    private static final String DELETE_RESEARCH_AGENCY_GROUP_FIELD = "UPDATE grailResearchAgencyGroup SET isActive = 0 where id = ?";
    
    private SynchroDAOUtil synchroDAOUtil;

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }

    @Override
    public List<MetaField> getEndMarketFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getAllEndMarketFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_ENDMARKET_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getEndMarketFieldsByRegion(Long id)
    {

        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_FIELDS_BY_REGION, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by region from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getEndMarketFieldsByArea(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_FIELDS_BY_AREA, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by area from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long saveEndMarketField(String name, Long region)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailEndMarketFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailEndMarketFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_ENDMARKET_FIELD, id, name, sortOrder);
            getSimpleJdbcTemplate().update(INSERT_REGION_MAPPING_FIELD, region, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return id;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateEndMarketField(MetaField endMarketField, Long region)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_ENDMARKET_FIELD, endMarketField.getName(), endMarketField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_MARKET, endMarketField.getId());
            getSimpleJdbcTemplate().update(INSERT_REGION_MAPPING_FIELD, region, endMarketField.getId());

        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    /**
     * Synchro Phase 5
     * @author kanwardeep.grewal
     */

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveEndMarketFieldOther(Long eid, Long approval, Long marketType, Long t20_t40_Type)
    {
        try{
            getSimpleJdbcTemplate().update(INSERT_ENDMARKET_OTHER_FIELD, eid, approval, marketType, t20_t40_Type);
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    /**
     * Synchro Phase 5
     * @author kanwardeep.grewal
     */
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateEndMarketFieldOther(MetaField endMarketField,  Long approval, Long marketType, Long t20_t40_Type)
    {
        try{
        	String sql = "SELECT count(*) FROM grailemothersmapping where eid = " + endMarketField.getId();
        	Long count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
        	if(count > 0)
        	{
        		getSimpleJdbcTemplate().update(UPDATE_ENDMARKET_OTHER_FIELDS, approval, marketType, t20_t40_Type,  endMarketField.getId());	
        	}
        	else
        	{
        		 getSimpleJdbcTemplate().update(INSERT_ENDMARKET_OTHER_FIELD, endMarketField.getId(), approval, marketType, t20_t40_Type);
        	}
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to update end market's other fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
          //  getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_MARKET, endMarketField.getId());
           // getSimpleJdbcTemplate().update(INSERT_REGION_MAPPING_FIELD, region, endMarketField.getId());

        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteEndMarketField(Integer id)
    {
    	
        try{
            getSimpleJdbcTemplate().update(DELETE_ENDMARKET_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortEndMarketField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_ENDMARKET_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortGrailResearchAgencyField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_RESEARCH_AGENCY_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for Research Agency fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }


    /**
     * Methodology Fields
     */
    @Override
    public List<MetaField> getMethodologyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_METHODOLOGY_FIELDS, methMetaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    /**
     * Methodology Fields
     */
    @Override
    public List<MetaField> getAllMethodologyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_METHODOLOGY_FIELDS, methMetaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public boolean isMethodologyTypeActive(final Long id) {
        boolean isActive = true;
        try {
            String sql = "SELECT isActive FROM grailMethodologyFields where id=?";
            Integer a =  getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sql, id);
            if(a != null && a.intValue() == 1) {
                isActive = true;
            } else {
                isActive = false;
            }
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return isActive;
    }

    @Override
    public List<MetaField> getSelectedInactiveMethodologyFields(final List<Long> ids) {
        List<MetaField> methodologies = Collections.emptyList();
        if(ids != null && ids.size() > 0) {
            try {
                String sql = "SELECT id,name, islessfrequent FROM grailMethodologyFields where isactive = 0 AND id in ("+StringUtils.join(ids,",")+")";
                methodologies = getSimpleJdbcTemplate().getJdbcOperations().query(sql, methMetaFieldRowMapper);
            } catch (DataAccessException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return methodologies;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveMethodologyField(MetaField metafield,  List<Long> groups)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailMethodologyFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailMethodologyFields");
        try{
        	String name = metafield.getName();
            getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_FIELD, id, name, (metafield.isLessFrequent()?1:0), (metafield.isBriefException()?1:0), 
            		(metafield.isProposalException()?1:0), (metafield.isAgencyWaiverException()?1:0),  (metafield.isRepSummaryException()?1:0), metafield.getBrandSpecific(), sortOrder);
            for(Long group : groups)
            {
                getSimpleJdbcTemplate().update(INSERT_METHDOLOGY_MAPPING_FIELD, id, group);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Methodology fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateMethodologyField(MetaField metaField, List<Long> ids)
    {

        try{
            getSimpleJdbcTemplate().update(UPDATE_METHODOLOGY_FIELD, metaField.getName(), (metaField.isLessFrequent()?1:0), (metaField.isBriefException()?1:0), 
            		(metaField.isProposalException()?1:0), (metaField.isAgencyWaiverException()?1:0),  (metaField.isRepSummaryException()?1:0), metaField.getBrandSpecific(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Methodology fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all exsiting mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_METHODOLOGY, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_METHDOLOGY_MAPPING_FIELD, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteMethodology(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_METHDOLOGY_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortMethodologyField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_METHDOLOGY_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getMethodologyGroupFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_METHODOLOGY_GROUP_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology Group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getMethodologiesByGroup(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_METHODOLOGY__FIELDS_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Group ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getAllMethodologiesByGroup(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_METHODOLOGY__FIELDS_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load All Methodology fields by Mythodology Group ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<MetaField> getMethodologiesByType(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_METHODOLOGY_MAPPING_BY_TYPE, methMetaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Type ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<MetaField> getMethodologyGrpsByType(Long id) {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_METHODOLOGY_GROUP_MAPPING_BY_TYPE, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Type ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<MetaField> getUnselectedMethodologiesForMethType() {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_UNSELECTED_METHODOLOGY_MAPPING_BY_TYPE, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Type ID from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<MetaField> getUnselectedMethodologyGroupsForType() {

        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_UNSELECTED_METHODOLOGY_GROUP_MAPPING_BY_TYPE, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Type ID from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long getMethodologyTypeByProposedMethodology(final Long id) {
        Long fieldId = null;
        try {
            fieldId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_METH_TYPE_BY_PROP_METHODOLOGY, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology type by Proposed Methodology from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fieldId;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Set<Long> getMethodologyTypesByProposedMethodologies(List<Long> ids) {

        Set<Long> fieldIds = Collections.emptySet();
        try {
            String sql = "SELECT mp.methodologytypeid from grailMethTypeMappingFields mp where mp.methodologyid in ("+StringUtils.join(ids,",")+") group by mp.methodologytypeid";
            List<Long> result = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class);
            if(result != null & result.size() > 0) {
                fieldIds = Sets.newHashSet(result);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology type by proposed methodology type ids from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fieldIds;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long getMethodologyTypeByGroup(final Long id) {
        Long fieldId = null;
        try {
            fieldId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_METH_TYPE_BY_GROUP, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology fields by Mythodology Type ID from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fieldId;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveMethodologyGroupField(String name, List<Long> methodologies)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailMethodologyGroupFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailMethodologyGroupFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_GROUP_FIELD, id, name, sortOrder);
            for(Long methodology : methodologies)
            {
                getSimpleJdbcTemplate().update(INSERT_METHDOLOGY_MAPPING_FIELD, methodology, id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Methodology Group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateMethodologyGroupField(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_METHODOLOGY_GROUP_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Methodology Group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_METHODOLOGYGROUP, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_METHDOLOGY_MAPPING_FIELD, id, metaField.getId());
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteMethodologyGroup(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_METHODOLOGY_GROUP_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortMethodologyGroupField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_METHODOLOGY_GROUP_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    /**
     * Products
     */
    @Override
    public List<MetaField> getProductFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_PRODUCT_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Product fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProductField(String name, List<Long> ids)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailProductFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailProductFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_PRODUCT_FIELD, id, name, sortOrder);
            for(Long brandId : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_BRAND_MAPPING_FIELD, id, brandId);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert product fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProductField(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_PRODUCT_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Methodology fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_PRODUCT, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_BRAND_MAPPING_FIELD, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProduct(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_PRODUCT_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortProductField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_PRODUCT_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getBrandFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_BRAND_FIELDS, brandMetaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load brand fields by product id from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getAllBrandFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_BRAND_FIELDS, brandMetaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load All brand fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getBrandFields(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_BRAND_FIELDS_BY_PRODUCT, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load brand fields by product id "+id+" from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public Integer getBrandId(final String name) {
        Integer id = 0;
        try {
            id = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(GET_BRAND_ID_BY_NAME, name);
        }
        catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return id;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveBrandField(String name, List<Long> ids)
    {
        Long brandId = synchroDAOUtil.nextSequenceID("id", "grailBrandFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailBrandFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_BRAND_FIELD, brandId, name, sortOrder);
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_BRAND_MAPPING_FIELD, id, brandId);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert brand fields for product id "+ids+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateBrandField(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_BRAND_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update brand fields for product type to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_BRAND, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_BRAND_MAPPING_FIELD, id, metaField.getId());
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteBrand(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_BRAND_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortBrandField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_BRAND_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getProductByBrandInclusion(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(PRODUCTS_BY_BRAND, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load brand fields by product id from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }


    @Override
    public List<MetaField> getSupplierGroupFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_SUPPLIER_GROUP_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load supplier group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveSupplierGroupField(String name,  List<Long> suppliers)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailSupplierGroupFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_SUPPLIER_GROUP_FIELD, id, name);
            for(Long supplier : suppliers)
            {
                getSimpleJdbcTemplate().update(INSERT_SUPPLIER_MAPPING_FIELD, id, supplier);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert supplier group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateSupplierGroupField(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_SUPPLIER_GROUP_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Supplier Group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getSupplierFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_SUPPLIER_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Supplier fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getSupplierFields(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_SUPPLIER_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Supplier fields for Supplier Group ID "+ id +" from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveSupplierField(String name, List<Long> groups)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailSupplierFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_SUPPLIER_FIELD, id, name);
            for(Long group : groups)
            {
                getSimpleJdbcTemplate().update(INSERT_SUPPLIER_MAPPING_FIELD, group, id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Supplier field "+name+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateSupplierField(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_SUPPLIER_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Supplier fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getFwSupplierGroupFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_FWSUPPLIER_GROUP_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load fw supplier group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveFwSupplierGroupField(String name,  List<Long> fWsuppliers)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailFwSupplierGFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_FWSUPPLIER_GROUP_FIELD, id, name);
            for(Long supplier : fWsuppliers)
            {
                getSimpleJdbcTemplate().update(INSERT_FWSUPPLIER_MAPPING_FIELD, id, supplier);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert fw supplier group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFwSupplierGroupField(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_FWSUPPLIER_GROUP_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update FW Supplier Group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getFwSupplierFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_FWSUPPLIER_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load FW Supplier fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getFwSupplierFields(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_FWSUPPLIER_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load FW Supplier fields for Supplier Group ID "+ id +" from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveFwSupplierField(String name, List<Long> fWgroups)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailFwSupplierFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_FWSUPPLIER_FIELD, id, name);
            for(Long group : fWgroups)
            {
                getSimpleJdbcTemplate().update(INSERT_FWSUPPLIER_MAPPING_FIELD, group, id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert FW Supplier field "+name+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFwSupplierField(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_FWSUPPLIER_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to FW update Supplier fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    /**
     *
     * Currency Fields
     */
    @Override
    public List<Currency> getCurrencyFields()
    {

        List<Currency> fields = new ArrayList<Currency>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_CURRENCY_FIELDS, currencyRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load currency fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<Currency> getGlobalCurrencyFields()
    {

        List<Currency> fields = new ArrayList<Currency>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_GLOBAL_CURRENCY_FIELDS, currencyRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Global currency fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<Currency> getNonGlobalCurrencyFields()
    {

        List<Currency> fields = new ArrayList<Currency>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_NON_GLOBAL_CURRENCY_FIELDS, currencyRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Non Global currency fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
//isglobal
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveCurrencyField(String name, String description, final String globalCurrency)
    {
    	int globalCurrency_int = 0;
    	
    	if(globalCurrency!=null && StringUtils.isNumeric(globalCurrency))
    	{
    		globalCurrency_int = Integer.parseInt(globalCurrency);
    	}
        Long id = synchroDAOUtil.nextSequenceID("id", "grailCurrencyFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailCurrencyFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_CURRENCY_FIELD, id, name, description, globalCurrency_int, sortOrder);
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert currency field "+name+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateCurrencyField(Currency currency)
    {    	
    	int isGlobalCurrency = ((currency.isGlobal())?1:0);
        try{
            getSimpleJdbcTemplate().update(UPDATE_CURRENCY_FIELD, currency.getName(), currency.getDescription(), isGlobalCurrency, currency.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Currency field to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteCurrencyField(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_CURRENCY_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortCurrencyField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_CURRENCY_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<CurrencyExchangeRate> getCurrencyExchangeRates() {
        List<CurrencyExchangeRate> exchangeRates = null;
        try {
            exchangeRates = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_EXCHANGE_RATES, exchangeRateRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return exchangeRates;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public CurrencyExchangeRate getCurrencyExchangeRate(Long id) {
        CurrencyExchangeRate exchangeRate = null;
        try {
            exchangeRate = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_EXCHANGE_RATE_BY_ID, exchangeRateRowMapper, id);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return exchangeRate;
    }

    private final ParameterizedRowMapper<CurrencyExchangeRate> exchangeRateRowMapper = new ParameterizedRowMapper<CurrencyExchangeRate>() {
        public CurrencyExchangeRate mapRow(ResultSet rs, int row) throws SQLException {
            CurrencyExchangeRate rate = new CurrencyExchangeRate();
            rate.setCurrencyId(rs.getLong("currencyId"));
            rate.setExchangeRate(rs.getBigDecimal("exchangerate"));
            return rate;
        }
    };

    /**
     *
     * Tendering Agency Fields
     */
    @Override
    public List<MetaField> getTAgencyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_TAGENCY_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Tendering Agency  fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;

    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveTAgencyField(String name)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailTAgencyFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_TAGENCY_FIELD, id, name);
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Tendering Agency field "+name+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateTAgencyField(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_TAGENCY_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Tendering Agency field to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    /**
     * Data Collection field queries
     */
    @Override
    public List<MetaField> getDataCollectionFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_DCOLLECTION_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load datac collection fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;

    }

    @Override
    public List<MetaField> getDataCollectionFields(Long type)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_DCOLLECTION_FIELDS_BY_TYPE, metaFieldRowMapper, type);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load datac collection fields for Methodology Type "+type+" from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;

    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveDataCollectionField(String name, List<Long> types)
    {
        /*	Long id = synchroDAOUtil.nextSequenceID("id", "grailDataCollectionFields");
          Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailDataCollectionFields");
           try{
                   getSimpleJdbcTemplate().update(INSERT_DCOLLECTION_FIELD, id, name, sortOrder);
              }
              catch (DataAccessException e) {
                  final String message = "Failed to insert Data Collection field "+name+" to database";
                  LOG.error(message, e);
                  throw new DAOException(message, e);
              }
            */

        Long id = synchroDAOUtil.nextSequenceID("id", "grailDataCollectionFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailDataCollectionFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_DCOLLECTION_FIELD, id, name, sortOrder);
            for(Long type : types)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, type, id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Data Collection fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateDataCollectionField(MetaField metaField, List<Long> types)
    {
        /* try{
                   getSimpleJdbcTemplate().update(UPDATE_DCOLLECTION_FIELD, metaField.getName(), metaField.getId());
              }
              catch (DataAccessException e) {
                  final String message = "Failed to update Data Collection field to database";
                  LOG.error(message, e);
                  throw new DAOException(message, e);
              }
              */
        try{
            getSimpleJdbcTemplate().update(UPDATE_DCOLLECTION_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Data Collection field to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_COLLECTION, metaField.getId());
            for(Long type : types)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, type, metaField.getId());
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteDataCollection(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_DCOLLECTION_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortDataCollectionField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_DCOLLECTION_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    /**
     * Methodology Type Fields
     */
    @Override
    public List<MetaField> getMethodologyTypeFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_MTYPE_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Methodology Type fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;

    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveMethodologyTypeField(String name, List<Long> collections)
    {
        /*
          Long id = synchroDAOUtil.nextSequenceID("id", "grailMethodologyTypeFields");
          Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailMethodologyTypeFields");
           try{
                   getSimpleJdbcTemplate().update(INSERT_MTYPE_FIELD, id, name, sortOrder);
              }
              catch (DataAccessException e) {
                  final String message = "Failed to insert Methodology Type field "+name+" to database";
                  LOG.error(message, e);
                  throw new DAOException(message, e);
              }
              */


        Long id = synchroDAOUtil.nextSequenceID("id", "grailMethodologyTypeFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailMethodologyTypeFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_MTYPE_FIELD, id, name, sortOrder);
            for(Long collection : collections)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, id, collection);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Methodology Type fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

    @Override
    public void saveMethodologyTypeField(String name, List<Long> collections, List<Long> methodologies) {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailMethodologyTypeFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailMethodologyTypeFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_MTYPE_FIELD, id, name, sortOrder);
            for(Long collection : collections)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, id, collection);
            }

            for(Long methodologyId : methodologies)
            {
                getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_TYPE_MAPPING_FIELD, id, methodologyId);
            }

//            for(Long methodologyId : methodologies)
//            {
//                getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_GROUP_TYPE_MAPPING_FIELD, id, methodologyId);
//            }


        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Methodology Type fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateMethodologyTypeField(MetaField metaField, List<Long> collections)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_MTYPE_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Methodology Type field to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_METHODOLOGYTYPE, metaField.getId());
            for(Long collection : collections)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, metaField.getId(), collection);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public void updateMethodologyTypeField(MetaField metaField, List<Long> collections, List<Long> methodologies) {
        try{
            getSimpleJdbcTemplate().update(UPDATE_MTYPE_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Methodology Type field to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        //Updates Mapping
        try{
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_METHODOLOGYTYPE, metaField.getId());
            for(Long collection : collections)
            {
                getSimpleJdbcTemplate().update(INSERT_COLLECTION_MAPPING_FIELD, metaField.getId(), collection);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
        }

//        try{
//            getSimpleJdbcTemplate().update(DELETE_METHODOLOGY_GROUP_TYPE_MAPPING_BY_TYPE, metaField.getId());
//            for(Long methId : methodologies)
//            {
//                getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_GROUP_TYPE_MAPPING_FIELD, metaField.getId(), methId);
//            }
//        }
//        catch (DataAccessException e) {
//            final String message = "exception " + e.getStackTrace();
//            LOG.error(message, e);
//        }

        try{
            getSimpleJdbcTemplate().update(DELETE_METHODOLOGY_TYPE_MAPPING_BY_TYPE, metaField.getId());
            for(Long methodologyId : methodologies)
            {
                getSimpleJdbcTemplate().update(INSERT_METHODOLOGY_TYPE_MAPPING_FIELD, metaField.getId(), methodologyId);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteMethodologyType(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_MTYPE_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete end market field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortMethodologyTypeField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_MTYPE_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for end market fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    private final ParameterizedRowMapper<MetaField> metaFieldRowMapper = new ParameterizedRowMapper<MetaField>() {
        public MetaField mapRow(ResultSet rs, int row) throws SQLException {
            MetaField field = new MetaField();
            field.setId(rs.getLong("id"));
            field.setName(rs.getString("name"));
            return field;
        }
    };
    
    private final ParameterizedRowMapper<MetaField> brandMetaFieldRowMapper = new ParameterizedRowMapper<MetaField>() {
        public MetaField mapRow(ResultSet rs, int row) throws SQLException {
            MetaField field = new MetaField();
            field.setId(rs.getLong("id"));
            field.setName(rs.getString("name"));
            field.setBrandType(rs.getInt("brandtype"));
            return field;
        }
    };
    
    private final ParameterizedRowMapper<MetaField> methMetaFieldRowMapper = new ParameterizedRowMapper<MetaField>() {
        public MetaField mapRow(ResultSet rs, int row) throws SQLException {
        	MetaField field = new MetaField();
            field.setId(rs.getLong("id"));
            field.setName(rs.getString("name"));
            field.setLessFrequent(rs.getBoolean("islessfrequent"));
            field.setBriefException(rs.getBoolean("briefexception"));
            field.setProposalException(rs.getBoolean("proposalexception"));
            field.setAgencyWaiverException(rs.getBoolean("agencywaiverexception"));
            field.setRepSummaryException(rs.getBoolean("repsummaryexception"));
            field.setBrandSpecific(rs.getInt("brandspecific"));
            return field;
        }
    };

    private final ParameterizedRowMapper<Currency> currencyRowMapper = new ParameterizedRowMapper<Currency>() {
        public Currency mapRow(ResultSet rs, int row) throws SQLException {
            Currency currency = new Currency();
            currency.setId(rs.getLong("id"));
            currency.setName(rs.getString("name"));
            currency.setDescription(rs.getString("description"));
            currency.setGlobal(rs.getBoolean("isglobal"));
            return currency;
        }
    };

    private final ParameterizedRowMapper<MetaFieldMapping> regionMappingMapper = new ParameterizedRowMapper<MetaFieldMapping>() {
        public MetaFieldMapping mapRow(ResultSet rs, int row) throws SQLException {
            MetaFieldMapping mapping = new MetaFieldMapping();
            mapping.setEid(rs.getLong("id"));
            mapping.setId(rs.getLong("regionid"));
            return mapping;
        }
    };

    private final ParameterizedRowMapper<MetaFieldMapping> areaMappingMapper = new ParameterizedRowMapper<MetaFieldMapping>() {
        public MetaFieldMapping mapRow(ResultSet rs, int row) throws SQLException {
            MetaFieldMapping mapping = new MetaFieldMapping();
            mapping.setEid(rs.getLong("id"));
            mapping.setId(rs.getLong("areaid"));
            return mapping;
        }
    };

    /**
     * Country Currency Row Mapper
     */
    private final ParameterizedRowMapper<MetaFieldMapping> countryCurrencyMapper = new ParameterizedRowMapper<MetaFieldMapping>() {
        public MetaFieldMapping mapRow(ResultSet rs, int row) throws SQLException {
            MetaFieldMapping mapping = new MetaFieldMapping();
            mapping.setEid(rs.getLong("endmarketid"));
            mapping.setId(rs.getLong("currencyid"));
            return mapping;
        }
    };

    /**
     * Regions
     */
    @Override
    public List<MetaField> getRegionFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_REGION_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Region fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getAllRegionFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_REGION_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Region fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveRegionField(String name, List<Long> ids)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailRegionFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailRegionFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_REGION_FIELD, id, name, sortOrder);
            /*Adding Region - EndMarket Mapping to End Market Table*/
            for(Long endmarketID : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_REGION_MAPPING_FIELD, id, endmarketID);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Region fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateRegionField(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_REGION_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Region fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        //Update Region - EndMarket Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_REGION, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_REGION_MAPPING_FIELD, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteRegion(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_REGION_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete region field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortRegionField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_REGION_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for region fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getRegionsByEndMarkets(List<Long> ids)
    {
        String LOAD_REGION_FIELDS_BY_ENDMARKETS = "SELECT region_t.id, region_t.name FROM grailRegionFields AS region_t JOIN grailRegionFieldMappingFields as mapping_t"+
                " ON region_t.id=mapping_t.regionid where mapping_t.endmarketid in ("+StringUtils.join(ids, ',')+") AND region_t.isActive = 1 order by region_t.sortorder asc";
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_REGION_FIELDS_BY_ENDMARKETS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Region fields By Endmarkets from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }



    /**
     * Areas
     */
    @Override
    public List<MetaField> getAreaFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_AREA_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Area fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveAreaField(String name, List<Long> ids)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailAreaFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailAreaFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_AREA_FIELD, id, name, sortOrder);
            for(Long endMarketID : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_AREA_MAPPING_FIELD, id, endMarketID);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Area fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateAreaField(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_AREA_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Area fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_AREA, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_AREA_MAPPING_FIELD, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteArea(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_AREA_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete area field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortAreaField(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_AREA_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for area fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getAreaByEndMarkets(List<Long> ids)
    {
        String LOAD_AREA_FIELDS_BY_ENDMARKETS = "SELECT id, name FROM grailAreaFields AS area_t JOIN grailAreaFieldMappingFields as mapping_t"+
                " ON area_t.id=mapping_t.areaid where mapping_t.endmarketid in ("+StringUtils.join(ids, ',')+") AND area_t.isActive = 1 order by area_t.sortorder asc";
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_AREA_FIELDS_BY_ENDMARKETS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Area fields By Endmarkets from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }


    /**
     * Job Titles
     */
    /**
     * Areas
     */
    @Override
    public List<MetaField> getJobTitles()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_JOBTITLE_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Job Title fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveJobTitle(String name)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailJobTitleFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailJobTitleFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_JOBTITLE_FIELD, id, name, sortOrder);
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Job Title fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateJobTitle(MetaField metaField)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_JOBTITLE_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Job Title fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteJobTitles(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_JOBTITLE_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete Job Title field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortJobTitles(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_JOBTITLE_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for Job Title fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }


    @Override
    public List<MetaFieldMapping> getEndMarketRegionMapping()
    {
        String LOAD_ENDMARKET_REGION_MAPPING = "SELECT id, regionid "+
                "FROM grailendmarketfields ge JOIN grailregionfieldmappingfields gm ON gm.endmarketid = ge.id WHERE ge.isActive = 1";
        List<MetaFieldMapping> mapping = new ArrayList<MetaFieldMapping>();
        try {
            mapping = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_REGION_MAPPING, regionMappingMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Endmarkets - Region mapping from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return mapping;
    }

    @Override
    public List<MetaFieldMapping> getEndMarketAreaMapping()
    {
        String LOAD_ENDMARKET_AREA_MAPPING = "SELECT id, areaid "+
                "FROM grailendmarketfields ge JOIN grailareafieldmappingfields gm ON gm.endmarketid = ge.id WHERE ge.isActive = 1";
        List<MetaFieldMapping> mapping = new ArrayList<MetaFieldMapping>();
        try {
            mapping = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_AREA_MAPPING, areaMappingMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Endmarkets - Area mapping from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return mapping;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void setCountryCurrencyMapping(Integer countryid, Integer currencyid)
    {
        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_CURRENCY_MAPPING_BY_COUNTRYID, countryid);
            getSimpleJdbcTemplate().update(INSERT_COUNTRY_CURRENCY_MAPPING, countryid, currencyid);

        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaFieldMapping> getCountryCurrencyMapping()
    {
        String LOAD_COUNTRY_CURRENCY_MAPPING = "SELECT endmarketid, currencyid "+
                "FROM grailCountryCurrency";

        List<MetaFieldMapping> mapping = new ArrayList<MetaFieldMapping>();
        try {
            mapping = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_COUNTRY_CURRENCY_MAPPING, countryCurrencyMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load country-currency mappings for all from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return mapping;
    }




    @Override
    @Transactional
    public Long setKantarReportType(final String name) {

        try {
            Long id = synchroDAOUtil.nextSequenceID("id", "grailkantarreporttype");
            Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailkantarreporttype");
            getSimpleJdbcTemplate().update(INSERT_KANTAR_REPORT_TYPE, id, name,1,0, sortOrder);
            return id;
        } catch (DataAccessException e) {
            final String message = "Failed to insert kantar report type fields to database";
            LOG.error(message, e);
            return null;
        }
    }

    @Override
    @Transactional
    public Long setKantarReportType(final String name, final boolean otherType) {

        try {
            Long id = synchroDAOUtil.nextSequenceID("id", "grailkantarreporttype");
            Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailkantarreporttype");
            getSimpleJdbcTemplate().update(INSERT_KANTAR_REPORT_TYPE, id, name, 1,otherType?1:0, sortOrder);
            return id;
        } catch (DataAccessException e) {
            final String message = "Failed to insert kantar report type fields to database";
            LOG.error(message, e);
            return null;
        }
    }

    @Override
    @Transactional
    public MetaField getKantarReportType(final String name) {
        MetaField reportType = null;
        try {
            reportType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_KANTAR_REPORT_TYPE_BY_NAME, metaFieldRowMapper, name.trim().toLowerCase());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return reportType;
    }

    @Override
    @Transactional
    public MetaField getKantarReportType(final Long id) {
        MetaField reportType = null;
        try {
            reportType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_KANTAR_REPORT_TYPE_BY_ID, metaFieldRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return reportType;
    }

    @Override
    @Transactional
    public List<MetaField> getKantarReportTypes() {
        List<MetaField> reportTypes = Collections.emptyList();
        try {
            reportTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_KANTAR_REPORT_TYPES, metaFieldRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return reportTypes;
    }

    @Override
    @Transactional
    public List<KantarReportTypeBean> getKantarReportTypeBeans() {
        List<KantarReportTypeBean> reportTypes = Collections.emptyList();
        try {
            reportTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_KANTAR_REPORT_TYPE_BEANS, kantarReportTypeRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return reportTypes;
    }

    private final ParameterizedRowMapper<KantarReportTypeBean> kantarReportTypeRowMapper = new ParameterizedRowMapper<KantarReportTypeBean>() {
        public KantarReportTypeBean mapRow(ResultSet rs, int row) throws SQLException {
            KantarReportTypeBean field = new KantarReportTypeBean();
            field.setId(rs.getLong("id"));
            field.setName(rs.getString("name"));
            field.setActive(rs.getBoolean("isActive"));
            field.setOtherType(rs.getBoolean("otherType"));
            field.setSortOrder(rs.getInt("sortorder"));
            return field;
        }
    };

    @Override
    @Transactional
    public Long updateKantarReportType(final Long id, final String name) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_KANTAR_REPORT_TYPE, name, id);
            return id;
        } catch (DataAccessException e) {
            final String message = "Failed to update kantar report type fields to database";
            LOG.error(message, e);
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteKantarReportType(Long id) {
        try {
            getSimpleJdbcTemplate().update(DELETE_KANTAR_REPORT_TYPE, id);
        } catch (DataAccessException e) {
            final String message = "Failed to update kantar report type fields to database";
            LOG.error(message, e);
        }
    }

    @Override
    @Transactional
    public void sortKantarReportTypes(Map<Long, Integer> orderMap) {
        try {
            for(Long id : orderMap.keySet()) {
                getSimpleJdbcTemplate().update(UPDATE_KANTAR_REPORT_TYPE_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for kantar button methodology type";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional
    public Long setKantarButtonMethodologyType(String name) {
        try {
            Long id = synchroDAOUtil.nextSequenceID("id", "grailkantarbtnmethodologytype");
            Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailkantarbtnmethodologytype");
            getSimpleJdbcTemplate().update(INSERT_KANTAR_BUTTON_METHODOLOGY_TYPE, id, name,sortOrder, 1);
            return id;
        } catch (DataAccessException e) {
            final String message = "Failed to insert kantar button methodology type fields to database";
            LOG.error(message, e);
            return null;
        }
    }

    @Override
    @Transactional
    public MetaField getKantarButtonMethodologyType(String name) {
        MetaField methodologyType = null;
        try {
            methodologyType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_KANTAR_BUTTON_METHODOLOGY_TYPE_BY_NAME,
                    metaFieldRowMapper, name.trim().toLowerCase());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyType;
    }

    @Override
    @Transactional
    public MetaField getKantarButtonMethodologyType(Long id) {
        MetaField methodologyType = null;
        try {
            methodologyType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_KANTAR_BUTTON_METHODOLOGY_TYPE_BY_ID,
                    metaFieldRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyType;
    }

    @Override
    @Transactional
    public List<MetaField> getKantarButtonMethodologyTypes() {
        List<MetaField> methodologyTypes = Collections.emptyList();
        try {
            methodologyTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_KANTAR_BUTTON_METHODOLOGY_TYPES, metaFieldRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyTypes;
    }

    @Override
    public List<MetaField> getAllKantarButtonMethodologyTypes() {
        List<MetaField> methodologyTypes = Collections.emptyList();
        try {
            methodologyTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_KANTAR_BUTTON_METHODOLOGY_TYPES, metaFieldRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyTypes;
    }

    @Override
    @Transactional
    public void updateKantarButtonMethodologyType(MetaField metaField) {
        if(metaField != null && metaField.getId() != null
                && (metaField.getId().longValue() > 0 || metaField.getId().longValue() == -100)) {
            try {
                getSimpleJdbcTemplate().update(UPDATE_KANTAR_BUTTON_METHODOLOGY_TYPE, metaField.getName(), metaField.getId());
            } catch (DataAccessException e) {
                final String message = "Failed to insert kantar button methodology type fields to database";
                LOG.error(message, e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteKantarButtonMethodologyType(Long id) {
        try{
            getSimpleJdbcTemplate().update(DELETE_KANTAR_BUTTON_METHODOLOGY_TYPE, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete kantar button methodology type from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional
    public void sortKantarButtonMethodologyTypes(Map<Long, Integer> orderMap) {
        try {
            for(Long id : orderMap.keySet()) {
                getSimpleJdbcTemplate().update(UPDATE_KANTAR_BUTTON_METHODOLOGY_TYPE_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for kantar button methodology type";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional
    public Long setGrailButtonMethodologyType(String name) {
        try {
            Long id = synchroDAOUtil.nextSequenceID("id", "grailbuttonmethodologytype");
            Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailbuttonmethodologytype");
            getSimpleJdbcTemplate().update(INSERT_GRAIL_BUTTON_METHODOLOGY_TYPE, id, name, sortOrder, 1);
            return id;
        } catch (DataAccessException e) {
            final String message = "Failed to insert grail button methodology type fields to database";
            LOG.error(message, e);
            return null;
        }
    }

    @Override
    @Transactional
    public MetaField getGrailButtonMethodologyType(String name) {
        MetaField methodologyType = null;
        try {
            methodologyType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_GRAIL_BUTTON_METHODOLOGY_TYPE_BY_NAME,
                    metaFieldRowMapper, name.trim().toLowerCase());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyType;
    }

    @Override
    @Transactional
    public MetaField getGrailButtonMethodologyType(Long id) {
        MetaField methodologyType = null;
        try {
            methodologyType = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_GRAIL_BUTTON_METHODOLOGY_TYPE_BY_ID,
                    metaFieldRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyType;
    }

    @Override
    @Transactional
    public List<MetaField> getGrailButtonMethodologyTypes() {
        List<MetaField> methodologyTypes = Collections.emptyList();
        try {
            methodologyTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_GRAIL_BUTTON_METHODOLOGY_TYPES, metaFieldRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyTypes;
    }

    @Override
    public List<MetaField> getAllGrailButtonMethodologyTypes() {
        List<MetaField> methodologyTypes = Collections.emptyList();
        try {
            methodologyTypes = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_GRAIL_BUTTON_METHODOLOGY_TYPES, metaFieldRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return methodologyTypes;
    }

    @Override
    @Transactional
    public void updateGrailButtonMethodologyType(MetaField metaField) {
        if(metaField != null && metaField.getId() != null
                && (metaField.getId().longValue() > 0 || metaField.getId().longValue() == -100)) {
            try {
                getSimpleJdbcTemplate().update(UPDATE_GRAIL_BUTTON_METHODOLOGY_TYPE, metaField.getName(), metaField.getId());
            } catch (DataAccessException e) {
                final String message = "Failed to insert kantar button methodology type fields to database";
                LOG.error(message, e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteGrailButtonMethodologyType(Long id) {
        try{
            getSimpleJdbcTemplate().update(DELETE_GRAIL_BUTTON_METHODOLOGY_TYPE, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete grail button methodology type from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional
    public void sortGrailButtonMethodologyTypes(Map<Long, Integer> orderMap) {
        try {
            for(Long id : orderMap.keySet()) {
                getSimpleJdbcTemplate().update(UPDATE_GRAIL_BUTTON_METHODOLOGY_TYPE_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for grail button methodology type";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
      
    /**
     * Methodology Fields
     */
    @Override
    public List<MetaField> getResearchAgencyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_RESEACRCH_AGECNY_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Research Agency fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    /**
     * Methodology Fields
     */
    @Override
    public List<MetaField> getAllResearchAgencyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_RESEACRCH_AGECNY_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load All Research Agency fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    /**
     * Synchro PHASE 5 
     * @author kanwardeep.grewal
     */
    
    /**
     * T20/T40 field DAO IMPLS
     */
    
    @Override
    public List<MetaField> getT20T40Fields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_T20T40_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load T20/T40 fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveT20T40Field(String name, List<Long> ids)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailT20T40MetaFields");
        Long sortOrder = synchroDAOUtil.nextSequenceID("sortOrder", "grailT20T40MetaFields");
        try{
            getSimpleJdbcTemplate().update(INSERT_T20T40_FIELD, id, name, sortOrder);
            for(Long endMarketID : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_T20T40_MAPPING_FIELD, id, endMarketID);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert T20/T40 fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateT20T40Field(MetaField metaField, List<Long> ids)
    {
        try{
            getSimpleJdbcTemplate().update(UPDATE_T20T40_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update T20/T40 fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        //Updates Mapping
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_T20T40, metaField.getId());
            for(Long id : ids)
            {
                getSimpleJdbcTemplate().update(INSERT_T20T40_MAPPING_FIELD, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteT20T40(Integer id)
    {
        try{
            getSimpleJdbcTemplate().update(DELETE_T20T40_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete T20/T40 field from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void sortT20T40Field(Map<Integer, Integer> orderMap)
    {
        try{
            for(Integer id : orderMap.keySet())
            {
                getSimpleJdbcTemplate().update(UPDATE_T20T40_SORTFIELD, orderMap.get(id), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to update sort order for area fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getT20T40ByEndMarkets(List<Long> ids)
    {
        String LOAD_T20T40_FIELDS_BY_ENDMARKETS = "SELECT id, name FROM grailT20T40MetaFields AS t_table JOIN grailT20T40MappingMetaFields as m_table"+
                " ON t_table.tid = m_table.tid where m_table.eid in ("+StringUtils.join(ids, ',')+") AND t_table.isActive = 1 order by t_table.sortorder asc";
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_T20T40_FIELDS_BY_ENDMARKETS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to T20/T40 fields By Endmarkets from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getEndMarketFieldsByT20T40(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ENDMARKET_FIELDS_BY_T20T40, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by T20/T40 from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
        @Override
    public List<MetaField> getResearchAgencyGroupFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_RESEACRCH_AGECNY_GROUP_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Research Agency Group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }
    
    @Override
    public List<MetaField> getResearchAgencyByGroup(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_RESEARCH_AGENCY_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Research Agency fields by Research Agency Group ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getAllResearchAgencyByGroup(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_RESEARCH_AGENCY_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load All Research Agency fields by Research Agency Group ID " + id + " from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }


    @Override
    public Integer getApprovalByEndmarket(Long eid)
    {
    	if(getApprovalByEndmarketCount(eid) < 1)
    	{
    		return new Integer(0);
    	}
        Integer approvalType = new Integer(-1);
        try {
        	approvalType =  getSimpleJdbcTemplate().getJdbcOperations().queryForInt(GET_APPROVAL_TYPE_BY_EM, eid);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by T20/T40 from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return approvalType;
    }
    
    @Override
    public Integer getMarketByEndmarket(Long eid)
    {
    	if(getApprovalByEndmarketCount(eid) < 1)
    	{
    		return new Integer(0);
    	}
    	
        Integer marketType = new Integer(-1);
        try {
        	marketType =  getSimpleJdbcTemplate().getJdbcOperations().queryForInt(GET_MARKET_TYPE_BY_EM, eid);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by T20/T40 from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return marketType;
    }
    
    @Override
    public Integer getT20_T40_ByEndmarket(Long eid)
    {
    	if(getApprovalByEndmarketCount(eid) < 1)
    	{
    		return new Integer(0);
    	}
        Integer t20_T40_Type = new Integer(-1);
        try {
        	t20_T40_Type =  getSimpleJdbcTemplate().getJdbcOperations().queryForInt(GET_T20_T40_TYPE_BY_EM, eid);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load end market fields by T20/T40 from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return t20_T40_Type;
    }
    
    
    public Long getApprovalByEndmarketCount(Long eid) {
        Long count = 0L;
        String GET_TOTAL_COUNT = "SELECT count(*) FROM grailemothersmapping where eid = " + eid;        
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_TOTAL_COUNT);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return count;
    }
    

	/**
	 * Grail Research Agency & Group Tables	
	 */

    @Override
    public List<MetaField> getGrailResearchAgencyGroupFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_GRAIL_RESEARCH_AGENCY_GROUP_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load grail research agency group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveGrailResearchAgencyGroupField(String name,  List<Long> agencies)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailResearchAgencyGroup");
        try{
            getSimpleJdbcTemplate().update(INSERT_GRAIL_RESEARCH_AGENCY_GROUP_FIELD, id, name);
            for(Long agency : agencies)
            {
                getSimpleJdbcTemplate().update(INSERT_GRAIL_RESEARCH_AGENCY_MAPPING_FIELD, id, agency);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert grail research agency group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateGrailResearchAgencyGroupField(MetaField metaField, List<Long> agencies)
    {
    	//TODO
        try{
            getSimpleJdbcTemplate().update(UPDATE_GRAIL_RESEARCH_AGENCY_GROUP_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update grail research agency group Group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_RESEARCH_AGENCY_GROUP, metaField.getId());
            for(Long id : agencies)
            {
                getSimpleJdbcTemplate().update(INSERT_MAPPING_BY_RESEARCH_AGENCY_GROUP, id, metaField.getId());
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<MetaField> getGrailResearchAgencyFields()
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_GRAIL_RESEARCH_AGENCY_FIELDS, metaFieldRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load grail research agency group fields from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Override
    public List<MetaField> getGrailResearchAgencyFields(Long id)
    {
        List<MetaField> fields = new ArrayList<MetaField>();
        try {
            fields = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_GRAIL_RESEARCH_AGENCY_BY_GROUP, metaFieldRowMapper, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load grail research agency group fields for grail research agency group ID "+ id +" from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return fields;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveGrailResearchAgencyField(String name, List<Long> groups)
    {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailResearchAgency");
        try{
            getSimpleJdbcTemplate().update(INSERT_GRAIL_RESEARCH_AGENCY_FIELD, id, name);
            for(Long group : groups)
            {
                getSimpleJdbcTemplate().update(INSERT_GRAIL_RESEARCH_AGENCY_MAPPING_FIELD, group, id);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to insert Supplier field "+name+" to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateGrailResearchAgencyField(MetaField metaField, List<Long> groups)
    {
    	//TODO
        try{
            getSimpleJdbcTemplate().update(UPDATE_GRAIL_RESEARCH_AGENCY_FIELD, metaField.getName(), metaField.getId());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update  research agency group fields to database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
        try{
            //Deletes all existing mapping for this object
            getSimpleJdbcTemplate().update(DELETE_MAPPING_BY_RESEARCH_AGENCY, metaField.getId());
            for(Long id : groups)
            {
                getSimpleJdbcTemplate().update(INSERT_MAPPING_BY_RESEARCH_AGENCY_GROUP, metaField.getId(), id);
            }
        }
        catch (DataAccessException e) {
            final String message = "exception " + e.getStackTrace();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteResearchAgency(Integer id)
    {
    	
        try{
            getSimpleJdbcTemplate().update(DELETE_RESEARCH_AGENCY_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete Research Agency from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteResearchAgencyGroup(Integer id)
    {
    	
        try{
            getSimpleJdbcTemplate().update(DELETE_RESEARCH_AGENCY_GROUP_FIELD, id);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete Research Agency Group from the database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
        
}
