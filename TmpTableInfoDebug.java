import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Favorites;
import com.example.demo.pojo.entity.Follows;
import com.example.demo.pojo.entity.SysOperLog;
import com.example.demo.pojo.entity.Thumbs_up;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

public class TmpTableInfoDebug {
    public static void main(String[] args) {
        dump(Author.class);
        dump(Favorites.class);
        dump(Thumbs_up.class);
        dump(Follows.class);
        dump(SysOperLog.class);
    }

    private static void dump(Class<?> entityClass) {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, entityClass.getSimpleName() + "Mapper.xml");
        assistant.setCurrentNamespace(entityClass.getName() + "Mapper");
        TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, entityClass);

        System.out.println("=== " + entityClass.getSimpleName() + " ===");
        System.out.println("table=" + tableInfo.getTableName());
        System.out.println("keyProperty=" + tableInfo.getKeyProperty() + ", keyColumn=" + tableInfo.getKeyColumn() + ", keyType=" + tableInfo.getKeyType());
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            System.out.println(fieldInfo.getProperty() + " -> " + fieldInfo.getColumn() + " (" + fieldInfo.getPropertyType().getName() + ")");
        }
        System.out.println();
    }
}
