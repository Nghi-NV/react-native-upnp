
#import <React/RCTBridgeModule.h>
#import <CocoaSSDP/SSDPServiceBrowser.h>
@interface RNUpnp : NSObject <RCTBridgeModule, SSDPServiceBrowserDelegate>

@property (nonatomic, weak) RCTBridge *bridge;
@end
  
