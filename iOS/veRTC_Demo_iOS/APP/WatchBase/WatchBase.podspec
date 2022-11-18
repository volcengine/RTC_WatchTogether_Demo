
Pod::Spec.new do |spec|
  spec.name         = 'WatchBase'
  spec.version      = '1.0.0'
  spec.summary      = 'veByteMusic APP'
  spec.description  = 'veByteMusic App ..'
  spec.homepage     = 'https://github.com/volcengine'
  spec.license      = { :type => 'MIT', :file => 'LICENSE' }
  spec.author       = { 'author' => 'volcengine rtc' }
  spec.source       = { :path => './'}
  spec.ios.deployment_target = '9.0'
  
  #spec.source_files = '**/*.{h,m,c,mm,a}'
 
  spec.resources = ['Resource/*.{lic}']
  spec.vendored_frameworks = 'WatchBase.framework'
end
