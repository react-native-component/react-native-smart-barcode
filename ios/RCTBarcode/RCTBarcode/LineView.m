
#import "LineView.h"
#import "UIColor+Hex.h"
//#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation LineView

- (id)initWithScannerLineColor:(NSString*)scannerLineColor frame:(CGRect)frame
{
    
    if ((self = [super initWithFrame:frame])) {
        self.scannerLineColor = scannerLineColor;
    }
    return self;
    
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    self.backgroundColor = [UIColor clearColor];
//    NSArray *colors = @[@0x00000000, @0x8800FF00, @0xFF00FF00, @0xFF00FF00, @0xFF00FF00, @0xFF00FF00, @0xFF00FF00, @0x8800FF00, @0x00000000];
//    NSLog(@"@0xFF00FF00 = %@", @0xFF00FF00);
    UIColor *scannerLineColor = [UIColor colorWithHexString:self.scannerLineColor];
    NSArray *colors = @[scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor, scannerLineColor];
    [self _drawGradientColor:context
                        rect:rect
                     options:kCGGradientDrawsBeforeStartLocation
                      colors:colors];
}

/**
 * 绘制背景色渐变的矩形，p_colors渐变颜色设置，集合中存储UIColor对象（创建Color时一定用三原色来创建）
 **/
- (void)_drawGradientColor:(CGContextRef)p_context rect:(CGRect)p_clipRect options:(CGGradientDrawingOptions)p_options colors:(NSArray *)p_colors {
    CGContextSaveGState(p_context);// 保持住现在的context
    CGContextClipToRect(p_context, p_clipRect);// 截取对应的context
    long colorCount = p_colors.count;
    int numOfComponents = 4;
    CGColorSpaceRef rgb = CGColorSpaceCreateDeviceRGB();
    CGFloat colorComponents[colorCount * numOfComponents];
    CGColorRef temcolorRef = nil;
    for (int i = 0; i < colorCount; i++) {
        
        if(i==0){
            temcolorRef = [UIColor clearColor].CGColor;
        }else if (i==colorCount-1) {
            temcolorRef = [UIColor clearColor].CGColor;
        }else{
//            temcolorRef = UIColorFromRGB([p_colors[i] integerValue]).CGColor;
            temcolorRef = ((UIColor *)p_colors[i]).CGColor;
        }
        
        const CGFloat *components = CGColorGetComponents(temcolorRef);
        for (int j = 0; j < numOfComponents; ++j) {
            colorComponents[i * numOfComponents + j] = components[j];
        }
    }
    CGGradientRef gradient =  CGGradientCreateWithColorComponents(rgb, colorComponents, NULL, colorCount);
    CGColorSpaceRelease(rgb);
    CGPoint startPoint = CGPointMake(0, 0);
    CGPoint endPoint = CGPointMake(p_clipRect.size.width, p_clipRect.size.height);
    CGContextDrawLinearGradient(p_context, gradient, startPoint, endPoint, p_options);
    
    
    
    CGGradientRelease(gradient);
    CGContextRestoreGState(p_context);// 恢复到之前的context
}

@end
