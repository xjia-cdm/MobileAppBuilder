@echo off

java -Xms512m -Xmx4096m -cp work;%APP_BUILDER_HOME%lib\appbuilder.jar;%APP_BUILDER_HOME%lib\translator.jar;%APP_BUILDER_HOME%lib\ext\groovy-all-2.2.1.jar;%APP_BUILDER_HOME%lib\ext\ant-1.8.2.jar;%APP_BUILDER_HOME%lib\ext\ant-launcher-1.8.2.jar;%APP_BUILDER_HOME%lib\ext\commons-lang3-3.1.jar;%APP_BUILDER_HOME%lib\ext\jyaml-1.3.jar xj.mobile.Main %1 %2 %3 %4 %5 %6 %7 %8
