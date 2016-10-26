
#import <UIKit/UIKit.h>

@interface LineView : UIView

@property (nonatomic, copy) NSString *scannerLineColor;

- (id)initWithScannerLineColor:(NSString*)scannerLineColor frame:(CGRect)frame;

@end
