
#import "RNUpnp.h"
#import <CocoaSSDP/SSDPService.h>
#import <CocoaSSDP/SSDPServiceTypes.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>

@implementation RNUpnp
{
  
  SSDPServiceBrowser *_browser;
  RCTPromiseResolveBlock callback;
}

@synthesize bridge = _bridge;
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (id)init {
  if ((self = [super init])) {
  }
  
  
  _browser = [[SSDPServiceBrowser alloc] initWithServiceType:SSDPServiceType_UPnP_MediaRenderer1];
  _browser.delegate = self;
  
  return self;
}
RCT_EXPORT_METHOD(initUPNP:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
  [_browser stopBrowsingForServices];
  [_browser startBrowsingForServices];
  callback = resolve;
}

RCT_EXPORT_METHOD(refresh) {
  [_browser stopBrowsingForServices];
  [_browser startBrowsingForServices];
  NSLog(@"SSDP refresh");
}

#pragma mark - SSDP browser delegate methods

- (void) ssdpBrowser:(SSDPServiceBrowser *)browser didNotStartBrowsingForServices:(NSError *)error {
  NSLog(@"SSDP Browser got error: %@", error);
}

- (void) ssdpBrowser:(SSDPServiceBrowser *)browser didFindService:(SSDPService *)service {
  NSLog(@"SSDP Browser found: %@", service);
  
  NSDictionary *event = @{
                          @"hosts": service.location.host,
                          };
  [self.bridge.eventDispatcher sendAppEventWithName:@"speaker-found" body:event];
}

- (void) ssdpBrowser:(SSDPServiceBrowser *)browser didRemoveService:(SSDPService *)service {
  NSLog(@"SSDP Browser removed: %@", service);
}

@end

