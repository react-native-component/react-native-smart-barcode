
#import "RCTBarcode.h"
#import "RCTBarcodeManager.h"



@interface RCTBarcodeManager ()

@end

@implementation RCTBarcodeManager

RCT_EXPORT_MODULE(RCTBarcode)

RCT_EXPORT_VIEW_PROPERTY(scannerRectWidth, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(scannerRectHeight, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(scannerRectTop, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(scannerRectLeft, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(scannerLineInterval, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(scannerRectCornerColor, NSString)

RCT_EXPORT_VIEW_PROPERTY(onBarCodeRead, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY(barCodeTypes, NSArray, RCTBarcode) {
//    NSLog(@"custom barCodeTypes -> %@", self.barCodeTypes);
    self.barCodeTypes = [RCTConvert NSArray:json];
}

- (UIView *)view
{
    self.session = [[AVCaptureSession alloc]init];
#if !(TARGET_IPHONE_SIMULATOR)
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
//    self.previewLayer.needsDisplayOnBoundsChange = YES;
    #endif
    
    if(!self.barcode){
        self.barcode = [[RCTBarcode alloc] initWithManager:self];
        [self.barcode setClipsToBounds:YES];
    }
    
    SystemSoundID beep_sound_id;
    NSString *path = [[NSBundle mainBundle] pathForResource:@"beep" ofType:@"wav"];
    if (path) {
        AudioServicesCreateSystemSoundID((__bridge CFURLRef)[NSURL fileURLWithPath:path],&beep_sound_id);
        self.beep_sound_id = beep_sound_id;
    }
    
    return self.barcode;
}

- (id)init {
    if ((self = [super init])) {
        self.sessionQueue = dispatch_queue_create("barCodeManagerQueue", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

- (void)initializeCaptureSessionInput:(NSString *)type {
    
//    NSLog(@"initializeCaptureSessionInput...");
    
    dispatch_async(self.sessionQueue, ^{
    
        [self.session beginConfiguration];
        
        NSError *error = nil;

        AVCaptureDevice *captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        
        if (captureDevice == nil) {
            return;
        }
        
        AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
        
        if (error || captureDeviceInput == nil) {
//            NSLog(@"%@", error);
            return;
        }
        
        [self.session removeInput:self.videoCaptureDeviceInput];

        
        if ([self.session canAddInput:captureDeviceInput]) {
            
//            NSLog(@"self.session canAddInput:captureDeviceInput...");
            
            [self.session addInput:captureDeviceInput];
            
            self.videoCaptureDeviceInput = captureDeviceInput;
            
//            self.metadataOutput.rectOfInterest = self.barcode.bounds;
//            [self.metadataOutput setMetadataObjectTypes:self.metadataOutput.availableMetadataObjectTypes];
//            [self.metadataOutput setMetadataObjectTypes:self.barCodeTypes];

        }
        
        [self.session commitConfiguration];
    });
}

RCT_EXPORT_METHOD(startSession) {
    #if TARGET_IPHONE_SIMULATOR
    return;
    #endif
    dispatch_async(self.sessionQueue, ^{
        
//        NSLog(@"self.metadataOutput = %@", self.metadataOutput);
        
        if(self.metadataOutput == nil) {
            
//            NSLog(@"self.metadataOutput = %@", self.metadataOutput);
            
            AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
            self.metadataOutput = metadataOutput;
        
            if ([self.session canAddOutput:self.metadataOutput]) {
                [self.metadataOutput setMetadataObjectsDelegate:self queue:self.sessionQueue];
                [self.session addOutput:self.metadataOutput];
//                  [metadataOutput setMetadataObjectTypes:self.metadataOutput.availableMetadataObjectTypes];
                [self.metadataOutput setMetadataObjectTypes:self.barCodeTypes];
            }
            
//            [metadataOutput setMetadataObjectTypes:@[AVMetadataObjectTypeEAN13Code,AVMetadataObjectTypeEAN8Code,AVMetadataObjectTypeCode128Code,AVMetadataObjectTypeQRCode]];
            
            //            [[NSNotificationCenter defaultCenter] addObserverForName:AVCaptureInputPortFormatDescriptionDidChangeNotification
            //                        object:nil
            //                        queue:[NSOperationQueue currentQueue]
            //                        usingBlock: ^(NSNotification *_Nonnull note) {
//                                    metadataOutput.rectOfInterest = [self.previewLayer metadataOutputRectOfInterestForRect:CGRectMake(80, 80, 160, 160)];
            //                                                          }];
            
//            NSLog(@"startSession set metadataOutput...");
        }
        
        [self.session startRunning];
        
        if(self.barcode.scanLineTimer != nil) {
            //设回当前时间模拟继续效果
            [self.barcode.scanLineTimer setFireDate:[NSDate date]];
        }
        
    });
}

RCT_EXPORT_METHOD(stopSession) {
    #if TARGET_IPHONE_SIMULATOR
    return;
    #endif
    dispatch_async(self.sessionQueue, ^{
        
        [self.session commitConfiguration];
        [self.session stopRunning];
        
        
        //设置大时刻来模拟暂停效果
        [self.barcode.scanLineTimer setFireDate:[NSDate distantFuture]];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0),
                       dispatch_get_main_queue(),
                       ^{
//                           NSLog(@"self.barcode.scanLine remove animation");
                           [self.barcode.scanLine.layer removeAllAnimations];
                       });

    });
}

- (void)endSession {
    #if TARGET_IPHONE_SIMULATOR
    return;
    #endif
    dispatch_async(self.sessionQueue, ^{
        self.barcode = nil;
        [self.previewLayer removeFromSuperlayer];
        [self.session commitConfiguration];
        [self.session stopRunning];
        [self.barcode.scanLineTimer invalidate];
        self.barcode.scanLineTimer = nil;
        for(AVCaptureInput *input in self.session.inputs) {
            [self.session removeInput:input];
        }

        for(AVCaptureOutput *output in self.session.outputs) {
            [self.session removeOutput:output];
        }
        self.metadataOutput = nil;
    });
}

//- (void)startSession {
//#if TARGET_IPHONE_SIMULATOR
//    return;
//#endif
//    dispatch_async(self.sessionQueue, ^{
//        
//        AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
//        if ([self.session canAddOutput:metadataOutput]) {
//            [metadataOutput setMetadataObjectsDelegate:self queue:self.sessionQueue];
//            [self.session addOutput:metadataOutput];
////            [metadataOutput setMetadataObjectTypes:self.barCodeTypes];
//            [metadataOutput setMetadataObjectTypes:@[AVMetadataObjectTypeEAN13Code,AVMetadataObjectTypeEAN8Code,AVMetadataObjectTypeCode128Code,AVMetadataObjectTypeQRCode]];
//            
////            [[NSNotificationCenter defaultCenter] addObserverForName:AVCaptureInputPortFormatDescriptionDidChangeNotification
////                        object:nil
////                        queue:[NSOperationQueue currentQueue]
////                        usingBlock: ^(NSNotification *_Nonnull note) {
////                        metadataOutput.rectOfInterest = [self.previewLayer metadataOutputRectOfInterestForRect:CGRectMake(80, 80, 160, 160)];
////                                                          }];
//            
//            self.metadataOutput = metadataOutput;
//            NSLog(@"startSession set metadataOutput...");
//        }
//        
//        [self.session startRunning];
//    });
//}

//- (void)stopSession {
//#if TARGET_IPHONE_SIMULATOR
//    return;
//#endif
//    dispatch_async(self.sessionQueue, ^{
//        self.barcode = nil;
//        [self.previewLayer removeFromSuperlayer];
//        [self.session commitConfiguration];
//        [self.session stopRunning];
//        for(AVCaptureInput *input in self.session.inputs) {
//            [self.session removeInput:input];
//        }
//        
//        for(AVCaptureOutput *output in self.session.outputs) {
//            [self.session removeOutput:output];
//        }
//    });
//}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection {
//    NSLog(@"captureOutput!!!");
    for (AVMetadataMachineReadableCodeObject *metadata in metadataObjects) {
//        NSLog(@"AVMetadataMachineReadableCodeObject!!!");
//        NSLog(@"type = %@, data = %@", metadata.type, metadata.stringValue);
        for (id barcodeType in self.barCodeTypes) {
            if ([metadata.type isEqualToString:barcodeType]) {
                if (!self.barcode.onBarCodeRead) {
                    return;
                }
                
                AudioServicesPlaySystemSound(self.beep_sound_id);
                
//                NSLog(@"type = %@, data = %@", metadata.type, metadata.stringValue);
                self.barcode.onBarCodeRead(@{
                                              @"data": @{
                                                        @"type": metadata.type,
                                                        @"code": metadata.stringValue,
                                              },
                                            });
            }
        }
    }
}





- (NSDictionary *)constantsToExport
{
    return @{
                @"barCodeTypes": @{
                     @"upce": AVMetadataObjectTypeUPCECode,
                     @"code39": AVMetadataObjectTypeCode39Code,
                     @"code39mod43": AVMetadataObjectTypeCode39Mod43Code,
                     @"ean13": AVMetadataObjectTypeEAN13Code,
                     @"ean8":  AVMetadataObjectTypeEAN8Code,
                     @"code93": AVMetadataObjectTypeCode93Code,
                     @"code128": AVMetadataObjectTypeCode128Code,
                     @"pdf417": AVMetadataObjectTypePDF417Code,
                     @"qr": AVMetadataObjectTypeQRCode,
                     @"aztec": AVMetadataObjectTypeAztecCode
                     #ifdef AVMetadataObjectTypeInterleaved2of5Code
                     ,@"interleaved2of5": AVMetadataObjectTypeInterleaved2of5Code
                     # endif
                     #ifdef AVMetadataObjectTypeITF14Code
                     ,@"itf14": AVMetadataObjectTypeITF14Code
                     # endif
                     #ifdef AVMetadataObjectTypeDataMatrixCode
                     ,@"datamatrix": AVMetadataObjectTypeDataMatrixCode
                     # endif
                }
            };
}


@end
