/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.jaxws_client;

import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod;

import javax.xml.bind.annotation.XmlElement;
import java.util.Map;
import java.util.Set;

import freemarker.template.TemplateModelException;

/**
 * The validator for the jaxws-client module.
 *
 * @author Ryan Heaton
 */
public class JAXWSClientValidator extends BaseValidator {

  private final Set<String> serverSideTypesToUse;
  private final ClientClassnameForMethod clientConversion;

  public JAXWSClientValidator(Set<String> serverSideTypesToUse, Map<String, String> packageConversions) {
    this.serverSideTypesToUse = serverSideTypesToUse;
    this.clientConversion = new ClientClassnameForMethod(packageConversions);
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = new ValidationResult();

    for (Element element : complexType.getElements()) {
      if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
        result.addError(element, "Because of a bug in JAXB, an Map property can't have and @XmlElement annotation, which is required for the JAXWS client. " +
          "So you're going to have to use @XmlJavaTypeAdapter to supply your own adapter for the Map.");
      }
    }

    if (!serverSideTypesToUse.isEmpty()) {
      try {
        if (!complexType.getQualifiedName().equals(clientConversion.convert(complexType))) {
          result.addError(complexType, "If you're using server-side types in your client library, you can't convert the name of "
            + complexType.getQualifiedName() + " to " + clientConversion.convert(complexType) + ".");
        }
      }
      catch (TemplateModelException e) {
        throw new IllegalStateException(e);
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    if (!serverSideTypesToUse.isEmpty()) {
      for (WebMethod webMethod : ei.getWebMethods()) {
        for (WebFault webFault : webMethod.getWebFaults()) {
          try {
            if (!webFault.getQualifiedName().equals(clientConversion.convert(webFault))) {
              result.addError(webFault, "If you're using server-side types in your client library, you can't convert the name of "
                + webFault.getQualifiedName() + " to " + clientConversion.convert(webFault) + ".");
            }
          }
          catch (TemplateModelException e) {
            throw new IllegalStateException(e);
          }
        }
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    if (!serverSideTypesToUse.isEmpty()) {
      try {
        if (!simpleType.getQualifiedName().equals(clientConversion.convert(simpleType))) {
          result.addError(simpleType, "If you're using server-side types in your client library, you can't convert the name of "
            + simpleType.getQualifiedName() + " to " + clientConversion.convert(simpleType) + ".");
        }
      }
      catch (TemplateModelException e) {
        throw new IllegalStateException(e);
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = super.validateEnumType(enumType);
    if (!serverSideTypesToUse.isEmpty()) {
      try {
        if (!enumType.getQualifiedName().equals(clientConversion.convert(enumType))) {
          result.addError(enumType, "If you're using server-side types in your client library, you can't convert the name of "
            + enumType.getQualifiedName() + " to " + clientConversion.convert(enumType) + ".");
        }
      }
      catch (TemplateModelException e) {
        throw new IllegalStateException(e);
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateRootElement(RootElementDeclaration elType) {
    ValidationResult result = super.validateRootElement(elType);
    if (!serverSideTypesToUse.isEmpty()) {
      try {
        if (!elType.getQualifiedName().equals(clientConversion.convert(elType))) {
          result.addError(elType, "If you're using server-side types in your client library, you can't convert the name of "
            + elType.getQualifiedName() + " to " + clientConversion.convert(elType) + ".");
        }
      }
      catch (TemplateModelException e) {
        throw new IllegalStateException(e);
      }
    }
    return result;
  }
}