package util;

import java.util.EnumSet;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import javafx.base.SessionFactoryTone;

public class DB_DropAndCreatTable {


	// 在hibernate.cfg.xml中配置<mapping class="entity.Terminal" />
	// 在实体类entity...中配置表名、主键、字段名等 ( 注意 主键不能使用  @GeneratedValue(strategy = GenerationType.IDENTITY) annotation)
	// 执行此run函数，将会在数据库中执行drop和create语句
	// 恢复在实体类entity...中的主键@GeneratedValue信息
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
