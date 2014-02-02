
def m4(s) {
  def v = 0
  synchronized (s) {
    try {
      v = Integer.parseInt(s)
    } catch (NumberFormatException e) {
      e.printStackTrace()
    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      v = 0
    }
  }
}

println m4('453')
