package dao.basedao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.eastsoft.json.AFNData;
import com.google.gson.reflect.TypeToken;


// �ο�http://www.jianshu.com/p/d62c2be60617
//ParameterizedType ��˵���������硰 ����<> �������ͣ���:Map<String,User>  List<SoftParameter>
// ֮ǰ���� List<SoftParameter> ���õ���  new TypeToken<List<AFNData>>() {}.getType()
// ����д������List<T>��Ч
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