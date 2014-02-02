
package pkg1.pkg2

@Singleton
class Test { 

  int a = 100, b = 200

}

println Test.instance.a
println Test.instance.b
