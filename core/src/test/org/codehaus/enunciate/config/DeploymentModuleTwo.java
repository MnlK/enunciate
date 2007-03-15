package org.codehaus.enunciate.config;

import org.codehaus.enunciate.modules.BasicDeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;

import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class DeploymentModuleTwo extends BasicDeploymentModule {

  String attribute;
  final HashMap<String, String> elementMap = new HashMap<String, String>();

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  @Override
  public String getName() {
    return "module2";
  }

  public void putElement(String elementName, String elementValue) {
    this.elementMap.put(elementName, elementValue);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new RuleSetBase() {
      public void addRuleInstances(Digester digester) {
        digester.addCallMethod("enunciate/modules/module2/element", "putElement", 2);
        digester.addCallParam("enunciate/modules/module2/element", 0, "name");
        digester.addCallParam("enunciate/modules/module2/element", 1);
      }
    };
  }

}