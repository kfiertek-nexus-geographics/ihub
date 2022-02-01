package org.bimrocket.ihub.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author realor
 */
public class ConfigPropertyHandler
{
  private final String name;
  private final boolean required;
  private final String description;
  private final Field field;
  private final String defaultValue;

  ConfigPropertyHandler(ConfigProperty property, Field field)
  {
    String propName = property.name();
    if (propName.length() == 0)
      propName = field.getName();
    this.name = propName;
    this.required = property.required();
    this.description = property.description();
    this.defaultValue = property.defaultValue();
    this.field = field;
    this.field.setAccessible(true);
  }

  public String getName()
  {
    return name;
  }

  public boolean isRequired()
  {
    return required;
  }

  public String getDescription()
  {
    return description;
  }

  public Field getField()
  {
    return field;
  }

  public void setValue(Object bean, Object value) throws Exception
  {
    if (value != null)
    {
      field.set(bean, value);
    }
    else if (Collection.class.isAssignableFrom(field.getType()))
    {
      ((Collection) field.get(bean)).clear();
    }
  }

  public Object getValue(Object bean) throws Exception
  {
    return field.get(bean);
  }

  public String getType()
  {
    String typeName = field.getType().getSimpleName();
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType)
    {
      ParameterizedType paramType = (ParameterizedType) genericType;
      typeName += "<";
      Type[] typeArgs = paramType.getActualTypeArguments();
      for (int i = 0; i < typeArgs.length; i++)
      {
        if (i > 0)
          typeName += ", ";
        typeName += ((Class) typeArgs[i]).getSimpleName();
      }
      typeName += ">";
    }
    return typeName;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(name);
    if (required)
    {
      buffer.append("*");
    }
    buffer.append(" : ").append(getType());
    return buffer.toString();
  }

  public static Map<String, ConfigPropertyHandler> findProperties(Class cls)
  {
    return findProperties(cls, null);
  }

  public static Map<String, ConfigPropertyHandler> findProperties(Class cls,
      Map<String, ConfigPropertyHandler> properties)
  {
    if (properties == null)
      properties = new HashMap<>();

    Class superClass = cls.getSuperclass();
    if (superClass != null && superClass != Object.class)
    {
      findProperties(superClass, properties);
    }

    Field[] fields = cls.getDeclaredFields();
    for (Field field : fields)
    {
      ConfigProperty property = field.getAnnotation(ConfigProperty.class);
      if (property != null)
      {
        ConfigPropertyHandler propHandler = new ConfigPropertyHandler(property,
            field);
        properties.put(propHandler.getName(), propHandler);
      }
    }

    return properties;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

}
