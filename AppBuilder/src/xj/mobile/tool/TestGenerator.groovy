package xj.mobile.tool

public class TestGenerator { 

  /*
   * Generating test cases for testing list view
   */

  /*
   * Prarmeters: 
   *	embedded: yes / no
   *    modal:    yes / no 
   *    entity:   yes / no
   *    section:  yes / no
   * 
   *    selection, action, next
   *    section, item 
   *    popup, view 
   *    data: yes / no
   */

  static final String outdir = 'test'
  static final String outfile = 'app_g01.madl'

  static void main(args) { 
	generateTest01()
	generateTest02()

  }

  static void generateTest01() { 
	def content = '''app(\'MADL Test Case G-01\') {

}
'''
	File f = new File(outdir + File.separator + outfile)
	f.text = content
  }

}
