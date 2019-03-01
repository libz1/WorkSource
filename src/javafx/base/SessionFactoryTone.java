package javafx.base;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.eastsoft.util.Debug;

import util.SoftParameter;
import util.Util698;

/**
 * hibernate session factory ��������
 *
 * @author xuky
 * @version 2017-02-22
 *
 */
public class SessionFactoryTone {

	private volatile static SessionFactoryTone uniqueInstance;
	private SessionFactory sessionFactory = null;
	private StandardServiceRegistry registry = null;
	private String xml = "";

	public static SessionFactoryTone getInstance() {
		if (uniqueInstance == null) {
			synchronized (SessionFactoryTone.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new SessionFactoryTone();
				}
			}
		}
		return uniqueInstance;
	}

	public static String getXML(){

		String fileName = System.getProperty("user.dir") + "\\" + "arc\\";

//		System.out.println("SessionFactoryTone getDBTYPE begin");
		String dbtype = SoftParameter.getInstance().getDBTYPE();
//		System.out.println("SessionFactoryTone getDBTYPE end");

		// ��JFXMain���е���
		//Map<String, String> map = Util698.praseXml(SessionFactoryTone.getXML());
//        String str = map.get("session-factory");

		String xml = "";

		// xuky 2018.10.25 �Ƚ��г��ģ��ϸ�ģ��жϣ�Ȼ���ٽ��ж̵��ж�
		if (dbtype.toLowerCase().indexOf("netdb2") >= 0)
			xml = "hibernate.cfg_PostgreSQL.xml";
		else if (dbtype.toLowerCase().indexOf("netdb") >= 0)
			xml = "hibernate.cfg_MySql.xml";
		else
			xml = "hibernate.cfg_SQLite.xml";

		// xuky 2018.10.24 �������� �������´���
		// changeVal(fileName + xml);
//		org.dom4j.DocumentException: Error writing to server Nested exception: Error writing to server
//		at org.dom4j.io.SAXReader.read(SAXReader.java:484)
//		at org.dom4j.io.SAXReader.read(SAXReader.java:264)
//		at javafx.base.SessionFactoryTone.changeVal(SessionFactoryTone.java:83)


		// <mapping class="produce.entity.ProduceLog2MES"/>
		return fileName + xml;
	}

	// xuky 2018.07.18 ��xml�ļ����������޸�
	// �ο�https://blog.csdn.net/qq_24065713/article/details/77970469
	public static void changeVal(String fileName){
		String property = "hbm2ddl.auto";
		String val = "update";
		String newTable = "produce.entity.ProduceLog2MES";
		try {
			Boolean dataChaned = false, find_data = false;
			Document doc=new SAXReader().read(new File(fileName));
			Element root=doc.getRootElement();
			List<Element> configurations = root.elements();
			// ��һ��<hibernate-configuration>�ı���
			for (Element configuration : configurations) {
				// �ڶ���<session-factory>�ı���
				List<Element> factorys = configuration.elements();
				for (Element factory : factorys) {
//					System.out.println(factory.attributeValue("name"));
//					System.out.println(factory.attributeValue("class"));
					String class_val = factory.attributeValue("class");
					if (class_val!=null){
						Util698.log(SessionFactoryTone.class.getName(), "factory.attributeValue_class:"+class_val, Debug.LOG_INFO);
						if (class_val.indexOf(newTable)>=0){
							Util698.log(SessionFactoryTone.class.getName(), "find_data = true:"+newTable, Debug.LOG_INFO);
							find_data = true;
						}
					}
					if (factory.attributeValue("name")!=null){
						if (factory.attributeValue("name").equals(property)){
							if (!factory.getText().equals(val)){
								factory.setText(val);
								dataChaned = true;
					            Util698.log(SessionFactoryTone.class.getName(), "��Ҫ�޸�"+property+"="+val, Debug.LOG_INFO);
							}
						}
					}
				}
				if (!find_data){
		            Util698.log(SessionFactoryTone.class.getName(), "��Ҫ����"+newTable, Debug.LOG_INFO);
					Element add = configuration.addElement("mapping");
					add.addAttribute("class", newTable);
				}
			}

			if (dataChaned || !find_data){
		        FileOutputStream out =new FileOutputStream(fileName);
		        // ָ���ı���д���ĸ�ʽ��
		        OutputFormat format=OutputFormat.createPrettyPrint();   //Ư����ʽ���пո���
		        format.setEncoding("UTF-8");
		        //1.����д������
		        XMLWriter writer=new XMLWriter(out,format);
		        //2.д��Document����
		        writer.write(doc);
		        //3.�ر���
		        writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
            Util698.log(SessionFactoryTone.class.getName(), "changeVal Exception "+e.getMessage(), Debug.LOG_INFO);
		}
	}

	private SessionFactoryTone() {
		File DBFile = null;
		String fileName = getXML();
		DBFile = new File(fileName);
		registry = new StandardServiceRegistryBuilder()
				.configure(DBFile)
				.build();
//		registry = new StandardServiceRegistryBuilder()
//				.configure(xml)
////				.configure("arc\\hibernate.cfg.xml") // configures settings from hibernate.cfg.xml
//				.build();
		try {
			sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
            Util698.log(SessionFactoryTone.class.getName(), "SessionFactoryTone Exception"+e.getMessage(), Debug.LOG_INFO);

			// The registry would be destroyed by the SessionFactory,
			// but we had trouble building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	public Session openSession(){
		return sessionFactory.openSession();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public StandardServiceRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(StandardServiceRegistry registry) {
		this.registry = registry;
	}

	public void close(){
		try {
			sessionFactory.close();
			StandardServiceRegistryBuilder.destroy( registry );
		} catch (Exception e) {
            Util698.log(SessionFactoryTone.class.getName(), "close Exception"+e.getMessage(), Debug.LOG_INFO);
		}
	}

	public static void main(String[] args) {
//		SessionFactoryTone.getInstance().buildAttrVal(4731);
//		DB.getInstance().buildAttrVal(4729);
		getXML();
	}

	public String getXml() {
		return xml;
	}



}
