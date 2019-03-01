package util;

import java.util.EnumSet;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import javafx.base.SessionFactoryTone;

public class DB_DropAndCreatTable {


	// ��hibernate.cfg.xml������<mapping class="entity.Terminal" />
	// ��ʵ����entity...�����ñ������������ֶ����� ( ע�� ��������ʹ��  @GeneratedValue(strategy = GenerationType.IDENTITY) annotation)
	// ִ�д�run���������������ݿ���ִ��drop��create���
	// �ָ���ʵ����entity...�е�����@GeneratedValue��Ϣ
	private static void run(){
//		StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder().configure().build();
		StandardServiceRegistry standardServiceRegistry = SessionFactoryTone.getInstance().getRegistry();
		Metadata metadata = new MetadataSources(standardServiceRegistry).buildMetadata();
		SchemaExport schemaExport = new SchemaExport();
		schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);
	}

	public static void main(String[] arg){
		DB_DropAndCreatTable.run();
	}

}
