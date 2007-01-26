package net.sf.enunciate.modules.docs;

import junit.framework.TestCase;
import static net.sf.enunciate.EnunciateTestUtil.getAllJavaFiles;
import static net.sf.enunciate.InAPTTestCase.getInAPTClasspath;
import static net.sf.enunciate.InAPTTestCase.getSamplesDir;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.modules.xfire_client.ClientLibraryArtifact;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestGenerateDocsXml extends TestCase {

  /**
   * Tests the generation of the documentation.
   */
  public void testGenerateDocsXML() throws Exception {
    DocumentationDeploymentModule module = new DocumentationDeploymentModule() {

      @Override
      protected void buildBase() throws IOException {
        //no-op
      }
    };

    module.setCopyright("myco");
    module.setTitle("mytitle");
    module.setSplashPackage("net.sf.enunciate.samples.docs.pckg1");
    EnunciateConfiguration config = new EnunciateConfiguration(Arrays.asList((DeploymentModule) module));
    Enunciate enunciate = new Enunciate(getAllJavaFiles(getSamplesDir()));
    enunciate.setConfig(config);
    enunciate.setTarget(Enunciate.Target.BUILD);
    enunciate.setClasspath(getInAPTClasspath());
    ClientLibraryArtifact artifact1 = new ClientLibraryArtifact("module1", "1", "lib1");
    artifact1.setDescription("my <b>marked up</b> description for artifact 1");
    artifact1.addFile(new File("1.1.xml"), "my description 1.1");
    artifact1.addFile(new File("1.2.xml"), "my description 1.2");
    ClientLibraryArtifact artifact2 = new ClientLibraryArtifact("module2", "2", "lib2");
    artifact2.setDescription("my <b>marked up</b> description for artifact 2");
    artifact2.addFile(new File("2.1.xml"), "my description 2.1");
    artifact2.addFile(new File("2.2.xml"), "my description 2.2");
    enunciate.addArtifact(artifact1);
    enunciate.addArtifact(artifact2);
    enunciate.execute();

    File docsXml = new File(enunciate.getGenerateDir(), "docs/docs.xml");
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document document = builder.parse(docsXml);
    XPath xpath = XPathFactory.newInstance().newXPath();

    assertEquals("Here is some package documentation. <child>text</child>", xpath.evaluate("/api-docs/documentation", document).trim());
    assertEquals("myco", xpath.evaluate("/api-docs/@copyright", document).trim());

    String packageDocsXPath = "/api-docs/packages/package[@id='%s']/documentation";
    assertEquals("Here is some package documentation. <child>text</child>", xpath.evaluate(String.format(packageDocsXPath, "net.sf.enunciate.samples.docs.pckg1"), document).trim());
    assertEquals("Here is some more package documentation.", xpath.evaluate(String.format(packageDocsXPath, "net.sf.enunciate.samples.docs.pckg2"), document).trim());

    String packageTagsXPath = "/api-docs/packages/package[@id='%s']/tag[@name='%s']";
    assertEquals("sometag value", xpath.evaluate(String.format(packageTagsXPath, "net.sf.enunciate.samples.docs.pckg2", "sometag"), document).trim());

    String typeDocsXPath = "/api-docs/data/schema[@namespace='%s']/types/type[@id='%s']/documentation";
    assertEquals("Text for EnumOne", xpath.evaluate(String.format(typeDocsXPath, "urn:pckg1", "net.sf.enunciate.samples.docs.pckg1.EnumOne"), document).trim());
    
    //todo: finish up this testing...

    File libsXml = new File(enunciate.getGenerateDir(), "docs/client-libraries.xml");
    document = builder.parse(libsXml);
    
    String libDescriptionXPath = "/client-libraries/library[@name='%s']/description";
    assertEquals("my <b>marked up</b> description for artifact 1", xpath.evaluate(String.format(libDescriptionXPath, "lib1"), document).trim());
    assertEquals("my <b>marked up</b> description for artifact 2", xpath.evaluate(String.format(libDescriptionXPath, "lib2"), document).trim());

    String fileDescriptionXPath = "/client-libraries/library[@name='%s']/files/file[@name='%s']";
    assertEquals("my description 1.1", xpath.evaluate(String.format(fileDescriptionXPath, "lib1", "1.1.xml"), document).trim());
    assertEquals("my description 1.2", xpath.evaluate(String.format(fileDescriptionXPath, "lib1", "1.2.xml"), document).trim());
    assertEquals("my description 2.1", xpath.evaluate(String.format(fileDescriptionXPath, "lib2", "2.1.xml"), document).trim());
    assertEquals("my description 2.2", xpath.evaluate(String.format(fileDescriptionXPath, "lib2", "2.2.xml"), document).trim());

    File indexHtml = new File(enunciate.getBuildDir(), "docs/index.html");
    document = builder.parse(indexHtml);
    
    assertEquals("mytitle", xpath.evaluate("/html/head/title", document).trim());
    assertEquals("EIOneService", xpath.evaluate("//font[@style='text-decoration:line-through;']/a", document).trim());
    assertEquals("text", xpath.evaluate("//child", document).trim());
  }

}