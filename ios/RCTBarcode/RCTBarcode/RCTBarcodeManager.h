
#import <React/RCTViewManager.h>
#import <AVFoundation/AVFoundation.h>

@class RCTBarcode;

@interface RCTBarcodeManager : RCTViewManager<AVCaptureMetadataOutputObjectsDelegate>

@property (nonatomic, strong) dispatch_queue_t sessionQueue;
@property (nonatomic, strong) AVCaptureSession *session;
@property (nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property (nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, strong) RCTBarcode *barcode;
@property (nonatomic, strong) NSArray* barCodeTypes;
@property (nonatomic, assign) SystemSoundID beep_sound_id;

- (void)initializeCaptureSessionInput:(NSString*)type;

- (void)startSession;
- (void)stopSession;
- (void)endSession;

@end
