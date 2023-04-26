Pod::Spec.new do |spec|
  spec.name         = 'FeedShareDemo'
  spec.version      = '1.0.0'
  spec.summary      = 'FeedShareDemo APP'
  spec.description  = 'FeedShareDemo App Demo..'
  spec.homepage     = 'https://github.com/volcengine'
  spec.license      = { :type => 'MIT', :file => 'LICENSE' }
  spec.author       = { 'author' => 'volcengine rtc' }
  spec.source       = { :path => './' }
  spec.ios.deployment_target = '9.0'
  
  spec.source_files = '**/*.{h,m,c,mm,a}'
  spec.resource_bundles = {
    'FeedShareDemo' => ['Resource/*.xcassets']
  }
  spec.pod_target_xcconfig = {'CODE_SIGN_IDENTITY' => ''}
  spec.resources = ['Resource/*.{plist,lic}']
  spec.prefix_header_contents = '#import "Masonry.h"',
                                '#import "ToolKit.h"',
                                '#import "FeedShareDemoConstants.h"'

  spec.dependency 'ToolKit'
  spec.dependency 'YYModel'
  spec.dependency 'Masonry'
  spec.dependency 'SDWebImage'
  spec.dependency 'TTSDK/Player-SR'

end
