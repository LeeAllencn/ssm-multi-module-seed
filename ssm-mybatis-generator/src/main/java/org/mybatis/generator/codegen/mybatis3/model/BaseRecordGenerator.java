/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.model;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BaseRecordGenerator extends AbstractJavaGenerator {

    public BaseRecordGenerator() {
        super();
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString(
                "Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);

        commentGenerator.addClassComment(topLevelClass, introspectedTable);

        // 设置实体类的基本继承类
//        FullyQualifiedJavaType superClass = getSuperClass();
        FullyQualifiedJavaType superClass = new FullyQualifiedJavaType("com.rocky.ssm.utils.entity.BaseEntity");
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass);
            
            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }
        
        String rootClass = getRootClass();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(rootClass, warnings)
                    .containsProperty(introspectedColumn)) {
                continue;
            }
            if ("java.lang.Short".equals(introspectedColumn.getFullyQualifiedJavaType().getBaseQualifiedName())) {
            	introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Integer"));
			}

            Field field = getJavaBeansField(introspectedColumn);
//            if ("id".equals(field.getName())) {
//            	continue;
//			}
            if (plugins.modelFieldGenerated(field, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }

            Method method = getJavaBeansGetter(introspectedColumn);
            // 格式化日期的返回格式
            if ("java.util.Date".equals(method.getReturnType().getBaseQualifiedName())) {
            	method.addJavaDocLine("@JsonSerialize(using = JsonDateSerializer.class)");
            	topLevelClass.addImportedType("com.rocky.ssm.utils.format.JsonDateSerializer");
            	topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonSerialize");
    		}
            if (plugins.modelGetterMethodGenerated(method, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addMethod(method);
            }

            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass,
                        introspectedColumn, introspectedTable,
                        Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }
            }
        }

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().modelBaseRecordClassGenerated(
                topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        
        TopLevelClass voClass = getVoClass(topLevelClass);
        answer.add(voClass);
        answer.add(getServiceClass(topLevelClass,voClass));
        answer.add(getControllerClass(topLevelClass,voClass));
        return answer;
    }

//    private FullyQualifiedJavaType getSuperClass() {
//        FullyQualifiedJavaType superClass;
//        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
//            superClass = new FullyQualifiedJavaType(introspectedTable
//                    .getPrimaryKeyType());
//        } else {
//            String rootClass = getRootClass();
//            if (rootClass != null) {
//                superClass = new FullyQualifiedJavaType(rootClass);
//            } else {
//                superClass = null;
//            }
//        }
//
//        return superClass;
//    }

    private boolean includePrimaryKeyColumns() {
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.setName(topLevelClass.getType().getShortName());
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        List<IntrospectedColumn> constructorColumns =
            includeBLOBColumns() ? introspectedTable.getAllColumns() :
                introspectedTable.getNonBLOBColumns();
            
        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
        }
        
        StringBuilder sb = new StringBuilder();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable
                    .getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();
        
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            sb.setLength(0);
            sb.append("this."); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" = "); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }
    
    private List<IntrospectedColumn> getColumnsInThisClass() {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns()) {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }
        
        return introspectedColumns;
    }

    // 设置扩展类
    private TopLevelClass getVoClass(TopLevelClass superClass){
    	CommentGenerator commentGenerator = context.getCommentGenerator();
    	FullyQualifiedJavaType type = new FullyQualifiedJavaType(
    			superClass.getType().getBaseQualifiedName().replace("entity", "vo") + "Vo");
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        commentGenerator.addClassComment(topLevelClass, introspectedTable);
        topLevelClass.setSuperClass(superClass.getType());
        topLevelClass.addImportedType(superClass.getType());
    	return topLevelClass;
    }

    // 设置service层的类、字段、方法
    private TopLevelClass getServiceClass(TopLevelClass superClass,TopLevelClass voClass){
    	CommentGenerator commentGenerator = context.getCommentGenerator();
    	
    	//类
    	FullyQualifiedJavaType type = new FullyQualifiedJavaType(
    			superClass.getType().getBaseQualifiedName().replace("entity", "service") + "Service");
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        commentGenerator.addClassComment(topLevelClass, introspectedTable);
        topLevelClass.addJavaDocLine("@Service");
        topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
//        topLevelClass.addImportedType(superClass.getType());
        topLevelClass.addImportedType(voClass.getType());
        FullyQualifiedJavaType superCls = new FullyQualifiedJavaType("com.rocky.ssm.utils.service.BaseService");
        topLevelClass.setSuperClass(superCls);
        topLevelClass.addImportedType(superCls);
        
        //字段
        FullyQualifiedJavaType fqjtdao = new FullyQualifiedJavaType(superClass.getType().getBaseQualifiedName().replace("entity", "repository") + "Dao");
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(fqjtdao);
        String shortName = superClass.getType().getShortName();
        field.setName((shortName.charAt(0)+"").toLowerCase()+shortName.substring(1)+"Dao");
        field.addJavaDocLine("@Autowired");
        topLevelClass.addField(field);
        topLevelClass.addImportedType(field.getType());
        
        //方法
        addJavaServiceMethod(topLevelClass, superClass, voClass, field);
    	return topLevelClass;
    }
    
    private void addJavaServiceMethod(TopLevelClass topLevelClass,TopLevelClass superClass,TopLevelClass voClass,Field field){
//        Method method1 = new Method();
//        method1.setVisibility(JavaVisibility.PUBLIC);
//        method1.setReturnType(new FullyQualifiedJavaType("int"));
//        method1.setName("saveOrUpdate");
//        method1.addParameter(new Parameter(superClass.getType(), "record"));
//        method1.addBodyLine("if (StringUtils.isBlank(record.getId())) {");
//        method1.addBodyLine("return "+field.getName()+".insert(record);");
//        method1.addBodyLine("} else {");
//        method1.addBodyLine("return "+field.getName()+".updateById(record);");
//        method1.addBodyLine("}");
//        topLevelClass.addMethod(method1);
        
//        Method method2 = new Method();
//        method2.setVisibility(JavaVisibility.PUBLIC);
//        method2.setReturnType(new FullyQualifiedJavaType("int"));
//        method2.setName("deleteById");
//        method2.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "id"));
//        method2.addBodyLine("return "+field.getName()+".deleteById(id);");
//        topLevelClass.addMethod(method2);
        
        Method method3 = new Method();
        method3.setVisibility(JavaVisibility.PUBLIC);
        method3.setReturnType(voClass.getType());
        method3.setName("findById");
        method3.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "id"));
        method3.addBodyLine("return "+field.getName()+".selectById(id);");
        topLevelClass.addMethod(method3);
//        method3.addJavaDocLine("/*");
//        method3.addJavaDocLine(" * 以下四个方法不重写使用父类默认方法没有业务逻辑");
//        method3.addJavaDocLine(" * ");
//        method3.addJavaDocLine("@Override");
//        method3.addJavaDocLine("protected int save(BaseEntity record) {");
//        method3.addJavaDocLine("	// 业务逻辑");
//        method3.addJavaDocLine("}");
//        method3.addJavaDocLine("");
//        method3.addJavaDocLine("@Override");
//        method3.addJavaDocLine("protected int update(BaseEntity record) {");
//        method3.addJavaDocLine("	// 业务逻辑");
//        method3.addJavaDocLine("}");
//        method3.addJavaDocLine("");
//        method3.addJavaDocLine("@Override");
//        method3.addJavaDocLine("protected int delete(BaseEntity record) {");
//        method3.addJavaDocLine("	// 业务逻辑");
//        method3.addJavaDocLine("}");
//        method3.addJavaDocLine("");
//        method3.addJavaDocLine("@Override");
//        method3.addJavaDocLine("protected void validate() {");
//        method3.addJavaDocLine("	// 业务逻辑");
//        method3.addJavaDocLine("}");
//        method3.addJavaDocLine("*/");
//        method3.addJavaDocLine("");
        
        Method method4 = new Method();
        method4.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType fqjtlist = new FullyQualifiedJavaType("java.util.List<"+voClass.getType().getShortName()+">");
        method4.setReturnType(fqjtlist);
        method4.setName("queryByPage");
        FullyQualifiedJavaType fqjtmap = new FullyQualifiedJavaType("java.util.Map<String, Object>");
        method4.addParameter(new Parameter(fqjtmap, "params"));
        FullyQualifiedJavaType fqjtpb = new FullyQualifiedJavaType("com.rocky.ssm.utils.paginator.domain.PageBounds");
        method4.addParameter(new Parameter(fqjtpb, "page"));
        method4.addBodyLine("return "+field.getName()+".selectByPage(params, page);");
        FullyQualifiedJavaType innerReturnType = fqjtlist.getTypeArguments().get(0);
        innerReturnType.setBaseQualifiedName(innerReturnType.getBaseQualifiedName().replace("entity", "vo"));
        innerReturnType.setPackageName(innerReturnType.getPackageName().replace("entity", "vo"));
        topLevelClass.addImportedType(fqjtmap);
        topLevelClass.addImportedType(fqjtpb);
        topLevelClass.addImportedType(fqjtlist);
        topLevelClass.addMethod(method4);
        
        Method method5 = new Method();
        method5.setVisibility(JavaVisibility.PROTECTED);
        FullyQualifiedJavaType fqjtSuper = new FullyQualifiedJavaType("com.rocky.ssm.utils.repository.IBaseDao");
        method5.setReturnType(fqjtSuper);
        method5.setName("getDao");
        method5.addBodyLine("return "+field.getName()+";");
        method5.addJavaDocLine("@Override");
        topLevelClass.addImportedType(fqjtSuper);
        topLevelClass.addMethod(method5);
    }

    // 设置controller层的类、字段、方法
    private TopLevelClass getControllerClass(TopLevelClass superClass,TopLevelClass voClass){
    	CommentGenerator commentGenerator = context.getCommentGenerator();
    	String shortName = superClass.getType().getShortName();
    	String lowerShortName = (shortName.charAt(0)+"").toLowerCase()+shortName.substring(1);
    	
    	//类
    	FullyQualifiedJavaType type = new FullyQualifiedJavaType(
    			superClass.getType().getBaseQualifiedName().replace("entity", "web.controller") + "Controller");
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        commentGenerator.addClassComment(topLevelClass, introspectedTable);
        topLevelClass.addJavaDocLine("@Controller");
        topLevelClass.addJavaDocLine("@RequestMapping(\"/base/"+lowerShortName+"\")");
        topLevelClass.addImportedType("java.util.List");
        topLevelClass.addImportedType("java.util.Map");
        topLevelClass.addImportedType("org.apache.shiro.authz.annotation.RequiresPermissions");
        topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        topLevelClass.addImportedType("org.springframework.stereotype.Controller");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMapping");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMethod");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.ResponseBody");
//        topLevelClass.addImportedType(superClass.getType());
        topLevelClass.addImportedType(voClass.getType());
        FullyQualifiedJavaType superCls = new FullyQualifiedJavaType("com.rocky.ssm.utils.web.controller.base.BaseController");
        topLevelClass.setSuperClass(superCls);
        topLevelClass.addImportedType(superCls);
        
        //字段
        FullyQualifiedJavaType fqjtdao = new FullyQualifiedJavaType(superClass.getType().getBaseQualifiedName().replace("entity", "service") + "Service");
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(fqjtdao);
        
        field.setName(lowerShortName+"Service");
        field.addJavaDocLine("@Autowired");
        topLevelClass.addField(field);
        topLevelClass.addImportedType(fqjtdao);
        
        //方法
        addJavaControllerMethod(topLevelClass, superClass, voClass, field);
    	return topLevelClass;
    }
    
	private void addJavaControllerMethod(TopLevelClass topLevelClass, TopLevelClass superClass, TopLevelClass voClass, Field field) {
		String shortName = superClass.getType().getShortName();
    	String lowerShortName = (shortName.charAt(0)+"").toLowerCase()+shortName.substring(1);
    	
		Method method1 = new Method();
		method1.setVisibility(JavaVisibility.PUBLIC);
		method1.setReturnType(new FullyQualifiedJavaType("String"));
		method1.setName("listPage");
		method1.addBodyLine("return \"base/"+lowerShortName+"/list\";");
		method1.addJavaDocLine("@RequiresPermissions(\""+lowerShortName+":search\")");
		method1.addJavaDocLine("@RequestMapping(method = RequestMethod.GET)");
		topLevelClass.addMethod(method1);
		
		Method method2 = new Method();
		method2.setVisibility(JavaVisibility.PUBLIC);
		method2.setReturnType(new FullyQualifiedJavaType("Object"));
		method2.setName("search");
		FullyQualifiedJavaType fqjtmap = new FullyQualifiedJavaType("java.util.Map<String,Object>");
		method2.addParameter(new Parameter(fqjtmap, "params", "@RequestParam"));
		FullyQualifiedJavaType fqjtpb = new FullyQualifiedJavaType("com.rocky.ssm.utils.paginator.domain.PageJqGrid");
		method2.addParameter(new Parameter(fqjtpb, "page"));
		method2.addBodyLine("List<"+voClass.getType().getShortName()+"> list = "+lowerShortName+"Service.queryByPage(params, page);");
		method2.addBodyLine("return list;");
		method2.addJavaDocLine("@RequiresPermissions(\""+lowerShortName+":search\")");
		method2.addJavaDocLine("@RequestMapping(value = \"/search\", method = RequestMethod.POST)");
		method2.addJavaDocLine("@ResponseBody");
		topLevelClass.addImportedType(fqjtpb);
		topLevelClass.addMethod(method2);
		
		Method method3 = new Method();
		method3.setVisibility(JavaVisibility.PUBLIC);
		method3.setName("edit");
		method3.addParameter(new Parameter(voClass.getType(), "record"));
		method3.addBodyLine(lowerShortName+"Service.edit(record);");
		method3.addJavaDocLine("@RequiresPermissions(\""+lowerShortName+":edit\")");
		method3.addJavaDocLine("@RequestMapping(value = \"/edit\", method = RequestMethod.POST)");
		method3.addJavaDocLine("@ResponseBody");
		topLevelClass.addMethod(method3);
		
		Method method4 = new Method();
		method4.setVisibility(JavaVisibility.PUBLIC);
		method4.setReturnType(new FullyQualifiedJavaType("Object"));
		method4.setName("find");
		method4.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "id"));
		method4.addBodyLine(voClass.getType().getShortName()+" record = "+lowerShortName+"Service.findById(id);");
		method4.addBodyLine("return record;");
		method4.addJavaDocLine("@RequiresPermissions(\""+lowerShortName+":search\")");
		method4.addJavaDocLine("@RequestMapping(value = \"/find\", method = RequestMethod.POST)");
		method4.addJavaDocLine("@ResponseBody");
		topLevelClass.addMethod(method4);
	}
    //end add by xuy on 2015-09-10
}
