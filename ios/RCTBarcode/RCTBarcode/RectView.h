
#import <UIKit/UIKit.h>

@interface RectView : UIView

@property (nonatomic,assign)CGRect scannerRect;
@property (nonatomic, copy) NSString *scannerRectCornerColor;

- (id)initWithScannerRect:(CGRect)scannerRect frame:(CGRect)frame scannerRectCornerColor:(NSString*)scannerRectCornerColor;

@end
