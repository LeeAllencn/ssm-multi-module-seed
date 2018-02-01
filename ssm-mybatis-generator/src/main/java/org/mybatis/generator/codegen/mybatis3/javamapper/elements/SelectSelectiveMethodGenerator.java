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
package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.*;

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author Kevin-Y.Xu
 * 
 */
public class SelectSelectiveMethodGenerator extends
        AbstractJavaMapperMethodGenerator {

    public SelectSelectiveMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("java.util.List<"+introspectedTable.getRules().calculateAllFieldsClass()+"Vo>");
        FullyQualifiedJavaType innerReturnType = returnType.getTypeArguments().get(0);
        innerReturnType.setBaseQualifiedName(innerReturnType.getBaseQualifiedName().replace("entity", "vo"));
        innerReturnType.setPackageName(innerReturnType.getPackageName().replace("entity", "vo"));
        method.setReturnType(returnType);
        importedTypes.add(returnType);

        method.setName(introspectedTable.getSelectSelectiveStatementId());

        //List<DemoVo> findAllPage(Map<String, Object> params, PageBounds pageBounds);
    	FullyQualifiedJavaType type = new FullyQualifiedJavaType("java.util.Map<String, Object>");
        importedTypes.add(type);
        Parameter parameter = new Parameter(type, "params");
        method.addParameter(parameter);
        
        type = new FullyQualifiedJavaType("com.rocky.ssm.utils.paginator.domain.PageBounds");
        importedTypes.add(type);
        parameter = new Parameter(type, "page");
        method.addParameter(parameter);
        
        addMapperAnnotations(interfaze, method);

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(
                method, interfaze, introspectedTable)) {
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        return;
    }
}
