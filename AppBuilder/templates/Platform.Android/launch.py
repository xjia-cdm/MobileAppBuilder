# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

print 'Connecting to device ...'
# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

# sets a variable with the package's internal name
package = '___PACKAGE___'

# sets a variable with the name of an Activity in the package
activity = '___PACKAGE___.___PROJECTNAMEASIDENTIFIER___'

# sets the name of the component to start
runComponent = package + '/' + activity

print 'Starting activity'
# Runs the component
device.startActivity(component=runComponent)

# Presses the Menu button
device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)