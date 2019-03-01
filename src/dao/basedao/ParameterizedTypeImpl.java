package dao.basedao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.eastsoft.json.AFNData;
import com.google.gson.reflect.TypeToken;


// 参考http://www.jianshu.com/p/d62c2be60617
//ParameterizedType 简单说来就是形如“ 类型<> ”的类型，如:Map<String,User>  List<SoftParameter>
// 之前对于 List<SoftParameter> 采用的是  new TypeToken<List<AFNData>>() {}.getType()
// 这种写法对于List<T>无效
public class ParameterizedTypeImpl implements ParameterizedType {
    private final Class raw;
    private final Type[] args;
    public ParameterizedTypeImpl(Class raw, Type[] args) {
        this.raw = raw;
        this.args = args != null ? args : new Type[0];
    }
    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }
    @Override
    public Type getRawType() {
        return raw;
    }
    @Override
    public Type getOwnerType() {return null;}
}