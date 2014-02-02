package share.space



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Share)
class ShareTests {

	void setUp() {
		Share.list()*.delete()
	}

	void testPersist() {
		new Share(name: "1", createdDate:new Date(), priority: "", status:"").save()
		new Share(name: "2", createdDate:new Date(), priority: "", status:"").save()
		new Share(name: "3", createdDate:new Date(), priority: "", status:"").save()
		new Share(name: "4", createdDate:new Date(), priority: "", status:"").save()
		new Share(name: "5", createdDate:new Date(), priority: "", status:"").save()
		
		assert 5 == Share.count()
		def actualShare = Share.findByName('1')
		assert actualShare
		assert '1' == actualShare.name
	}

  void testToString() {
  	def s = new Share(name: "Code sample")
  	assert "Code sample" == s.name
  }
}
