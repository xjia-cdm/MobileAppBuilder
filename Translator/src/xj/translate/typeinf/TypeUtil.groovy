package xj.translate.typeinf

import groovyjarjarasm.asm.Type;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*;

import static org.codehaus.groovy.ast.ClassHelper.*

import xj.translate.common.* 

import static xj.translate.Logger.* 

class TypeUtil { 

  public static class Null { }

  public static final ClassNode NULL_TYPE = new ClassNode(Null.class);

  public static final ClassNode Number_TYPE = ClassHelper.make(Number.class);

  public static boolean hasGenericsTypes(MethodNode methodNode) {
    if (methodNode.getGenericsTypes() != null && 
	methodNode.getGenericsTypes().length > 0) return true;
    if (methodNode.isStatic()) return false;
    ClassNode clazz = methodNode.getDeclaringClass();
    while (clazz != null) {
      if (clazz.getGenericsTypes() != null && 
	  clazz.getGenericsTypes().length > 0) return true;
      if (clazz.isStaticClass()) break;
      clazz = clazz.getDeclaringClass();
    } //while (clazz != null);
    return false;
  }

  public static boolean isAssignableFrom(ClassNode to, ClassNode from) {
    if (from == null) return true;
    if (from == TypeUtil.NULL_TYPE) return true;

    if (to.equals(from) ||
	to.equals(OBJECT_TYPE) ||
	to.equals(boolean_TYPE) ||
	to.equals(Boolean_TYPE)) return true;
    //if (!from.implementsInterface(TypeUtil.TMAP) &&
    //!from.implementsInterface(TypeUtil.TLIST)) return true;

    to = TypeUtil.wrapSafely(to);
    from = TypeUtil.wrapSafely(from);
    if (to == from) return true;

    if(from.equals(CLASS_Type)) {
      final GenericsType[] gt = from.getGenericsTypes();
      if(gt != null && TypeUtil.isAssignableFrom(to, gt[0].getType())) {
	return true;
      }
    }

    if (TypeUtil.isNumericalType(to)) {
      if (TypeUtil.isNumericalType(from)
	  || to.equals(Character_TYPE) && (from.equals(STRING_TYPE) || from.equals(Character_TYPE)))
      return true;
    } else if (to.equals(Character_TYPE)) {
      if (TypeUtil.isNumericalType(from) || from.equals(STRING_TYPE))
	return true;
    } else if (to.equals(STRING_TYPE)) {
      if (TypeUtil.isNumericalType(from) || from.equals(STRING_TYPE) ||
	  isDirectlyAssignableFrom(GSTRING_TYPE, from)) {
	return true;
      }
    } else if (to.isArray() && from.implementsInterface(TypeUtil.COLLECTION_TYPE)) {
      return isAssignableFrom(to.getComponentType(), from.getComponentType());
    } else if (to.isArray() && from.isArray()) {
      return isAssignableFrom(to.getComponentType(), from.getComponentType());
    }

    /*
    if (from.implementsInterface(TMAP))
      return TypeUtil.isDirectlyAssignableFrom(to, TypeUtil.LINKED_HASH_MAP_TYPE);

    if (from.implementsInterface(TLIST))
      return TypeUtil.isDirectlyAssignableFrom(to, TypeUtil.ARRAY_LIST_TYPE);

    if (from.implementsInterface(TTHIS)) {
      while (from != null) {
	if (isDirectlyAssignableFrom(to, from)) return true;
	if (from.isStaticClass()) break;
	from = from.getDeclaringClass();
      }
      return false;
    }
    */

    //if (isReferenceUnboxing(to, from)) return true;

    return isDirectlyAssignableFrom(to, from);
  }

  //**************
  public static boolean isDirectlyAssignableFrom(ClassNode to, ClassNode from) {
    if (to.equals(OBJECT_TYPE))
      return !ClassHelper.isPrimitiveType(from);
    if (from == null) 
      return true;
    if (to.isArray() && from.isArray()) 
      return isDirectlyAssignableFrom(to.getComponentType(), from.getComponentType());
    
    return from == TypeUtil.NULL_TYPE || 
           from.isDerivedFrom(to) ||
           to.isInterface() && implementsInterface(to, from);

           //(from.isDerivedFrom(to) && (!from.implementsInterface(TCLOSURE) || to.equals(ClassHelper.CLOSURE_TYPE))) || 

  }

  public static boolean isConvertibleFrom(ClassNode t1, ClassNode t2) {
    if (t1.isArray() && t2.isArray()) 
      return isConvertibleFrom(t1.getComponentType(), t2.getComponentType());

	/*
        if (t1 instanceof ClosureClassNode) {
            if ("groovy.lang.Closure".equals(t2.getName())) return true;
        }  else if (t2 instanceof ClosureClassNode) {
            if ("groovy.lang.Closure".equals(t1.getName())) return true;
        }
	*/

    t1 = wrapSafely(t1);
    t2 = wrapSafely(t2);
    return isAssignableFrom(t1, t2) || 
           canHaveCommonSubtype(t1, t2) || 
	   areTypesDirectlyConvertible(t1, t2);
  }

  private static boolean canHaveCommonSubtype(ClassNode t1, ClassNode t2) {
      //return (t1.isInterface() && (t2.getModifiers() & Opcodes.ACC_FINAL) == 0) ||
      //         (t2.isInterface() && (t1.getModifiers() & Opcodes.ACC_FINAL) == 0);
    return t1.isInterface() || t2.isInterface()
  }

  public static boolean areTypesDirectlyConvertible(ClassNode t1, ClassNode t2) {
    if (t1.isArray() && t2.isArray()) 
      return areTypesDirectlyConvertible(t1.getComponentType(), t2.getComponentType());
    t1 = wrapSafely(t1);
    t2 = wrapSafely(t2);
    return isDirectlyAssignableFrom(t1, t2) || isDirectlyAssignableFrom(t2, t1) || canHaveCommonSubtype(t1, t2);
  }

  private static boolean implementsInterface(ClassNode type, ClassNode type1) {
    return type1.implementsInterface(type);
  }

  public static boolean isBooleanType(ClassNode paramType) {
    return paramType == boolean_TYPE ||
           paramType == Boolean_TYPE
  }

  public static boolean isNumericalType(ClassNode paramType) {
    return paramType == byte_TYPE || 
           paramType == short_TYPE || 
	   paramType == int_TYPE || 
	   paramType == float_TYPE || 
	   paramType == long_TYPE || 
	   paramType == double_TYPE || 
	   paramType == char_TYPE || 
	   paramType == Byte_TYPE || 
	   paramType == Short_TYPE || 
	   paramType == Integer_TYPE || 
	   paramType == Float_TYPE ||
	   paramType == Long_TYPE || 
	   paramType == Double_TYPE || 
	   paramType == BigDecimal_TYPE || 
	   paramType == BigInteger_TYPE || 
	   paramType == Character_TYPE ||
	   paramType == Number_TYPE;
  }

  /* type1 is a super type of type2 */
  public static boolean isSuperType(ClassNode type1, ClassNode type2) { 
    if (type2 == null || type2 == NULL_TYPE) return true;
    if (type1 == OBJECT_TYPE) return true; 

    if (type1 && type2) { 
      if (isNumericalType(type1) && isNumericalType(type2)) { 
	if (type1 == Number_TYPE) return true
	int ord1 = getNumericalTypeOrder(type1)
	int ord2 = getNumericalTypeOrder(type2)
	return ord1 > ord2
      } else { 
	return type2.isDerivedFrom(type1)
      }
    }
    return false
  }

  public static boolean isNumericAssignableType(ClassNode type1, ClassNode type2) { 
    if (type1 && type2) { 
      if (isNumericalType(type1) && isNumericalType(type2)) { 
	if (type1 == Number_TYPE) return true
	int ord1 = getNumericalTypeOrder(type1)
	int ord2 = getNumericalTypeOrder(type2)
	return ord1 > ord2
      }
    }
    return false
  }

  public static int getNumericalTypeOrder(ClassNode type) { 
    if (type && isNumericalType(type)) { 
      if (type == byte_TYPE || type == Byte_TYPE) return 1
      else if (type == short_TYPE || type == Short_TYPE || 
	       type == char_TYPE || type == Character_TYPE) return 2 
      else if (type == int_TYPE || type == Integer_TYPE) return 4
      else if (type == long_TYPE || type == Long_TYPE) return 8 
      else if (type == BigInteger_TYPE) return 16
      else if (type == float_TYPE || type == Float_TYPE) return 128
      else if (type == double_TYPE || type == Double_TYPE) return 256
      else if (type == BigDecimal_TYPE) return 1024 
      else return 2048
    }
    return 0
  }

  public static ClassNode commonType(ArrayList<ClassNode> types) {
    if (types.size == 0) 
      return null;
	  
    def result = types.get(0);
    for (int i=1; i<types.size; i++) { 
      result = commonType(result, types.get(i))
    }
    return result
  }
  
  public static ClassNode commonType(ClassNode type1, ClassNode type2) {
    if (type1 == null || type2 == null)
      throw new RuntimeException("Internal Error");
      
    if (type1.equals(type2))
      return type1;
      
    if (type1 == NULL_TYPE)
      return type2;
      
    if (type2 == NULL_TYPE)
      return type1;

    if (type1.equals(ClassHelper.OBJECT_TYPE) || type2.equals(ClassHelper.OBJECT_TYPE))
      return ClassHelper.OBJECT_TYPE;
      
    type1 = TypeUtil.wrapSafely(type1);
    type2 = TypeUtil.wrapSafely(type2);
    
    if (isNumericalType(type1) && isNumericalType(type2)) {
      if (type1.equals(ClassHelper.BigDecimal_TYPE) || type2.equals(ClassHelper.BigDecimal_TYPE))
	return ClassHelper.BigDecimal_TYPE;
      if (type1.equals(ClassHelper.Double_TYPE) || type2.equals(ClassHelper.Double_TYPE))
	return ClassHelper.double_TYPE;
      if (type1.equals(ClassHelper.Float_TYPE) || type2.equals(ClassHelper.Float_TYPE))
	return ClassHelper.float_TYPE;
      if (type1.equals(ClassHelper.BigInteger_TYPE) || type2.equals(ClassHelper.BigInteger_TYPE))
	return ClassHelper.BigInteger_TYPE
      if (type1.equals(ClassHelper.Long_TYPE) || type2.equals(ClassHelper.Long_TYPE))
	return ClassHelper.long_TYPE;
      if (type1.equals(ClassHelper.Integer_TYPE) || type2.equals(ClassHelper.Integer_TYPE))
	return ClassHelper.int_TYPE;
      return Number_TYPE;
    }
    
    final Set<ClassNode> allTypes1 = getAllTypes(type1);
    final Set<ClassNode> allTypes2 = getAllTypes(type2);
    
      /*
      for (ClassNode cn : allTypes1)
      if (allTypes2.contains(cn)) {
	cn = getSubstitutedType(cn, cn, type1);
	return cn;
      }
      */
    return ClassHelper.OBJECT_TYPE;
  }

  public static boolean isBigDecimal(ClassNode type) {
    return type == BigDecimal_TYPE;
  }
  
  public static boolean isBigInteger(ClassNode type) {
    return type == BigInteger_TYPE;
  }

  public static boolean isBigNumber(ClassNode type) {
    return type == BigDecimal_TYPE || type == BigInteger_TYPE;
  }  
  
  public static boolean isFloatingPoint(ClassNode type) {
    return type == double_TYPE || type == float_TYPE;
  }
  
  public static boolean isLong(ClassNode type) {
    return type == long_TYPE;
    }
  
  public static boolean isNumber(ClassNode type) {
    return type.equals(TypeUtil.Number_TYPE);
  }

  public static boolean isIntegerType(ClassNode type) { 
    return type == byte_TYPE || 
           type == short_TYPE || 
	   type == int_TYPE || 
	   type == long_TYPE || 
	   type == char_TYPE || 
	   type == Byte_TYPE || 
	   type == Short_TYPE || 
	   type == Integer_TYPE || 
	   type == Long_TYPE || 
	   type == Character_TYPE; 
  }

  public static ClassNode getMathType(ClassNode l, ClassNode r) {
    l = getUnwrapper(l);
    r = getUnwrapper(r);
    
    if (isNumber(l) || isNumber(r)) {
      return TypeUtil.Number_TYPE;
    }
    if (isFloatingPoint(l) || isFloatingPoint(r)) {
      return double_TYPE;
    }
    if (isBigDecimal(l) || isBigDecimal(r)) {
      return BigDecimal_TYPE;
    }
    if (isBigInteger(l) || isBigInteger(r)) {
      return BigInteger_TYPE;
    }
    if (isLong(l) || isLong(r)) {
      return long_TYPE;
    }
    return int_TYPE;
  }
  
  static LinkedHashSet<ClassNode> getAllTypes(ClassNode cn) {
    LinkedHashSet<ClassNode> set = new LinkedHashSet<ClassNode>();
    
    LinkedList<ClassNode> ifaces = new LinkedList<ClassNode>();
    if (!cn.isInterface()) {
      for (ClassNode c = cn; c != null && !c.equals(ClassHelper.OBJECT_TYPE); c = c.getSuperClass()) {
	set.add(c);
	ifaces.addAll(Arrays.asList(cn.getInterfaces()));
      }
    } else {
      ifaces.add(cn);
    }
    
    while (!ifaces.isEmpty()) {
      ClassNode iface = ifaces.removeFirst();
      set.add(iface);
      ifaces.addAll(Arrays.asList(iface.getInterfaces()));
    }
    
    set.add(ClassHelper.OBJECT_TYPE);
    return set;
  }

  public static ClassProcessor findTypeDef(ClassNode type) { 
    return findTypeDef(type?.nameWithoutPackage)
  }

  public static ClassProcessor findTypeDef(String type) { 
    if (type) {
       return ModuleProcessor.classMap.get(type)
    }
    return null
  }

  public static boolean isEnum(ClassNode type) { 
    if (type) { 
      def cp = ModuleProcessor.classMap.get(type.nameWithoutPackage)
      if (cp) { 
	return cp.classNode.isEnum()
      } else { 
	return type.isEnum()
      }
    }
    return false
  }
  
  public static ClassNode wrapSafely(ClassNode type) {
    if (ClassHelper.isPrimitiveType(type)) return ClassHelper.getWrapper(type);
    else return type;
  }

  public static boolean isStringExpressionType(String op) {
    switch(op) { 
    case '+' : case '-' : case '*' :
    case '$++' : case '++$' : 
    case '$--' : case '--$' :
      return true;
      //TODO: check if these apply
      //'**'  : '{0}.pow({1})',
      //'/'   : '{0}.divide({1})',
      //'%'   : '{0}.remainder({1})',
    default:
      return false;
    }
  }

  public static boolean isListType(ClassNode cn)  {
    if (cn == LIST_TYPE || cn == RANGE_TYPE)
      return true

    if (cn.name == 'List') 
      return true
    
    if (isAssignableFrom(LIST_TYPE, cn) || isAssignableFrom(RANGE_TYPE, cn))
      return true
    
    LinkedHashSet<ClassNode> types = getAllTypes(cn)
    if (types.contains(ClassHelper.LIST_TYPE))
      return true
    if (cn.clazz?.name == "java.util.List" || cn.name?.contains("java.util.list"))
      return true
    
    return false
  }

  public static boolean isMapType(ClassNode cn)  {
    if (cn == MAP_TYPE) 
      return true

    if (cn.name == 'Map') 
      return true
    
    if (isAssignableFrom(MAP_TYPE, cn))
      return true
    
    LinkedHashSet<ClassNode> types = getAllTypes(cn)
    if (types.contains(ClassHelper.MAP_TYPE))
      return true
    if (cn.clazz?.name == "java.util.Map")
      return true
    
    return false
  }

  public static boolean allObjectTypes(GenericsType[] generics) { 
    if (generics && generics.length > 0) { 
      for (int i=0; i<generics.length; i++) {
	if (generics[i].name != 'Object') return false
      }
    }
    return true
  }

  public static String getNameWithGenerics(String oldName, GenericsType[] generics) {
    int end = oldName.indexOf("<")
    if (end == -1)
      end = oldName.length()
    String newName = oldName.substring(0, end)
    if (generics.length == 0)
      return newName

    newName += "<"
    for (int i=0; i<generics.length; i++) {
      if (i>0)
	newName += ","
      newName += generics[i].name
    }
    newName += ">"
    
    return newName
  }

}
