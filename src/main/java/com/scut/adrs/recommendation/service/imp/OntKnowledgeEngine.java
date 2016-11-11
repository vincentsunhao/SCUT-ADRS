package com.scut.adrs.recommendation.service.imp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Restriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scut.adrs.domain.BodySigns;
import com.scut.adrs.domain.Disease;
import com.scut.adrs.domain.Pathogeny;
import com.scut.adrs.domain.Symptom;
import com.scut.adrs.recommendation.dao.OntParserDao;
import com.scut.adrs.recommendation.exception.UnExistRdfException;
import com.scut.adrs.recommendation.service.PreDiaKnowledgeEngine;
import com.scut.adrs.util.ontDaoUtils;

@Service
public class OntKnowledgeEngine implements PreDiaKnowledgeEngine{

	static String NS=ontDaoUtils.getNS();
	@Autowired
	OntParserDao ontParserDao;

	public OntParserDao getOntParserDao() {
		return ontParserDao;
	}

	public void setOntParserDao(OntParserDao ontParserDao) {
		this.ontParserDao = ontParserDao;
	}
	/**
	 * 根据症状去获取疾病的算法
	 */
	@Override
	public Set<Disease> getRelativeDiseaseBySymptom(Symptom symptom) throws UnExistRdfException {
		if(symptom==null) return null;
    	List<Restriction> reList =ontParserDao.parseRestriction(symptom.getSymptomName());
    	Set<String> interDiseaseName=new HashSet<String>();
    	Set<Disease> interDisease=new HashSet<Disease>();
    	for(Restriction re:reList){
    		if(re.isAllValuesFromRestriction()){
    			interDiseaseName.add(re.asAllValuesFromRestriction().getAllValuesFrom().getURI());
    		}
    		if(re.isSomeValuesFromRestriction()){
    			interDiseaseName.add(re.asSomeValuesFromRestriction().getSomeValuesFrom().getURI());
    		}
    	}
    	for(String rdf:interDiseaseName){
    		interDisease.add(new Disease(rdf));
    	}
    	return interDisease;

	}

	@Override
	public Set<Symptom> getRelativeSymptomByDisease(Disease disease) {
		String property="是表现";
    	//找到对应的所有约束
    	Set<Restriction> reSet=this.ontParserDao.getRestriction(property, disease.getDiseaseName());
    	//存放所有约束相关的子类的集合
    	Set<OntClass> ontClassSet = new HashSet<OntClass>();
    	//存放要构造成交互症状的集合
    	Set<Symptom> interSymptom=new HashSet<Symptom>();
    	 for (Restriction re : reSet) {
             for (Iterator<OntClass> i = re.listSubClasses(true); i.hasNext();) {
                 OntClass ontClass = i.next();
                 if (!ontClassSet.contains(ontClass))
                 ontClassSet.add(ontClass);
             }
         }
    	 //开始构造交互症状
    	for(OntClass ontclass:ontClassSet){
    		interSymptom.add(new Symptom(ontclass.getURI()));
    	}
    	return interSymptom;
	}

	@Override
	public Set<Pathogeny> getRelativePathogenyByDisease(Disease disease) {
		//定义算法属性
    	String property="导致";
    	//找到对应的所有约束
    	Set<Restriction> reSet=this.ontParserDao.getRestriction( property, disease.getDiseaseName());
    	//存放所有约束相关的子类的集合
    	Set<OntClass> ontClassSet = new HashSet<OntClass>();
    	//存放要构造成交互病因的集合
    	Set<Pathogeny> interPathogeny=new HashSet<Pathogeny>();
    	 for (Restriction re : reSet) {
             for (Iterator<OntClass> i = re.listSubClasses(true); i.hasNext();) {
                 OntClass ontClass = i.next();
                 ontClassSet.add(ontClass);
             }
         }
    	 //开始构造交互病因
    	for(OntClass ontclass:ontClassSet){
    		interPathogeny.add(new Pathogeny(ontclass.getURI()));
    	}
    	//这里由问题有待解决
    	return interPathogeny;
	}

	@Override
	public Set<Disease> getRelativeDiseaseByDisease(Disease disease) {
		//引发
    	String property="引发";
    	//找到对应的所有约束
    	Set<Restriction> reSet=ontParserDao.getRestriction(property, disease.getDiseaseName());
    	//存放所有约束相关的子类的集合
    	Set<OntClass> ontClassSet = new HashSet<OntClass>();
    	//存放要构造成交互症状的集合
    	Set<Disease> interDisease=new HashSet<Disease>();
    	for (Restriction re : reSet) {
            for (Iterator<OntClass> i = re.listSubClasses(true); i.hasNext();) {
                OntClass ontClass = i.next();
                if (!ontClassSet.contains(ontClass))
                    System.out.println("这里在打印病："+ontClass.getLocalName());
                ontClassSet.add(ontClass);
            }
        }
    	//开始构造交互疾病
    	for(OntClass ontclass:ontClassSet){
    		interDisease.add(new Disease(ontclass.getURI()));
    	}
    	return interDisease;
	}

	@Override
	public Set<BodySigns> getRelativeBodySignsByDisease(Disease disease) {
		Set<BodySigns> interBodySigns=new HashSet<BodySigns>();
		// TODO Auto-generated method stub
		return interBodySigns;
	}
	

	

}