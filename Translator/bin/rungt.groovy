  
def dir = 'test/groovy/trans'
def infile = 'TestReturns01'
//def infile = 'TestStrings01'

def p = "bin/gt -header=no ${dir}/${infile}.groovy".execute()
//p.waitFor()

//println p.text
p.text

//System.exit(0)