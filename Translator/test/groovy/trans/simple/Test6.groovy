for (int i = 0; i < 10; i++) {
    println i
}
for (def i = 0; i < 10; i++) {
    println i
}
for (def x in 1 .. 10) {
    println x
}
for (def x in [ 1, 2, 3 ]) {
    println x
}
for (def x in [ 'Chicago', 'New York', 'London' ]) {
    println x
}
for (def x in [ 1 : 'a', 2 : 'b', 3 : 'c' ]) {
    println x
}