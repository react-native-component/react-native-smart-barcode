#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "RCTBarcodeManager.h"
#import "LineView.h"
#import "RectView.h";


@interface RCTBarcode : UIView

@property (nonatomic,strong)NSTimer *scanLineTimer;
@property (nonatomic,strong)LineView *scanLine;
@property (nonatomic,assign)CGRect scannerRect;
@property (nonatomic, copy) RCTBubblingEventBlock onBarCodeRead;
@property (nonatomic, assign) NSInteger scannerRectWidth;
@property (nonatomic, assign) NSInteger scannerRectHeight;
@property (nonatomic, assign) NSInteger scannerRectTop;
@property (nonatomic, assign) NSInteger scannerRectLeft;
@property (nonatomic, assign) NSInteger scannerLineInterval;
@property (nonatomic, copy) NSString *scannerRectCornerColor;

- (id)initWithManager:(RCTBarcodeManager*)manager;
- (void)moveUpAndDownLine;
- (void)createTimer;

@end
