println m(2.2)
println m(5)

println f1(2.3)
println f1("hi")
println f1([1,2,3])

println f2(1, 5)
println f2("he", "llo")
println f2([1,2,3], 4)

println f3(1, 2.5)
println f3(1, 2)
println f3("abc", "def")
//f3([4,5,6], [1,2,3])


def f1(x) {  x * 2 }
def f2(x, y) { x + y }
def f3(x, y) { x < y ? x : y }

def m(x) {
	if (x < 1)
		return 1;
	else
		return x;
}
