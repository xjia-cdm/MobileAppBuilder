
boolean b = true 
def x = 1, y = 2, z = 3

println (x + y * z)
println ((x + y) * z)

println 100 + (b ? x : y)
println ((b ? x : y) + 100)
println (b ? x : y + 100)