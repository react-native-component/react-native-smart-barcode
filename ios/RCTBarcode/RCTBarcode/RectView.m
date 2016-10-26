
#import "RectView.h"
#import "UIColor+Hex.h"

@implementation RectView


- (id)initWithScannerRect:(CGRect)scannerRect frame:(CGRect)frame scannerRectCornerColor:(NSString*)scannerRectCornerColor
{
    
    if ((self = [super initWithFrame:frame])) {
        self.scannerRect = scannerRect;
        self.scannerRectCornerColor = scannerRectCornerColor;
    }
    return self;
    
}

- (void)drawRect:(CGRect)rect {
    
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    [self addScreenFillRect:ctx rect:rect];
    [self addCenterClearRect:ctx rect:self.scannerRect];
    [self addWhiteRect:ctx rect:self.scannerRect];
    [self addCornerLineWithContext:ctx rect:self.scannerRect];
}


#pragma mark 画背景
- (void)addScreenFillRect:(CGContextRef)ctx rect:(CGRect)rect {
    CGContextSetRGBFillColor(ctx, 40 / 255.0,40 / 255.0,40 / 255.0,0.5);
    CGContextFillRect(ctx, rect);   //draw the transparent layer
}
#pragma mark 扣扫描框
- (void)addCenterClearRect :(CGContextRef)ctx rect:(CGRect)rect {
    CGContextClearRect(ctx, rect);  //clear the center rect  of the layer
}
#pragma mark 画框的白线
- (void)addWhiteRect:(CGContextRef)ctx rect:(CGRect)rect {
    CGContextStrokeRect(ctx, rect);
    CGContextSetRGBStrokeColor(ctx, 1, 1, 1, 1);
    CGContextSetLineWidth(ctx, 0.8);
    CGContextAddRect(ctx, rect);
    CGContextStrokePath(ctx);
}
#pragma mark 画框的四个角
- (void)addCornerLineWithContext:(CGContextRef)ctx rect:(CGRect)rect{
    
//    UIColor *cornerLineColor = [UIColor redColor];
//    NSLog(@"self.scannerRectCornerColor=%@", self.scannerRectCornerColor);
    UIColor *cornerLineColor = [UIColor colorWithHexString:self.scannerRectCornerColor];
    
    //画四个边角
    CGContextSetLineWidth(ctx, 2);
    [self setStrokeColor:cornerLineColor withContext:ctx];
    
    //左上角
    CGPoint poinsTopLeftA[] = {
        CGPointMake(rect.origin.x+0.7, rect.origin.y),
        CGPointMake(rect.origin.x+0.7 , rect.origin.y + 15)
    };
    
    CGPoint poinsTopLeftB[] = {CGPointMake(rect.origin.x, rect.origin.y +0.7),CGPointMake(rect.origin.x + 15, rect.origin.y+0.7)};
    [self addLine:poinsTopLeftA pointB:poinsTopLeftB ctx:ctx];
    
    //左下角
    CGPoint poinsBottomLeftA[] = {CGPointMake(rect.origin.x+ 0.7, rect.origin.y + rect.size.height - 15),CGPointMake(rect.origin.x +0.7,rect.origin.y + rect.size.height)};
    CGPoint poinsBottomLeftB[] = {CGPointMake(rect.origin.x , rect.origin.y + rect.size.height - 0.7) ,CGPointMake(rect.origin.x+0.7 +15, rect.origin.y + rect.size.height - 0.7)};
    [self addLine:poinsBottomLeftA pointB:poinsBottomLeftB ctx:ctx];
    
    //右上角
    CGPoint poinsTopRightA[] = {CGPointMake(rect.origin.x+ rect.size.width - 15, rect.origin.y+0.7),CGPointMake(rect.origin.x + rect.size.width,rect.origin.y +0.7 )};
    CGPoint poinsTopRightB[] = {CGPointMake(rect.origin.x+ rect.size.width-0.7, rect.origin.y),CGPointMake(rect.origin.x + rect.size.width-0.7,rect.origin.y + 15 +0.7 )};
    [self addLine:poinsTopRightA pointB:poinsTopRightB ctx:ctx];
    
    CGPoint poinsBottomRightA[] = {CGPointMake(rect.origin.x+ rect.size.width -0.7 , rect.origin.y+rect.size.height+ -15),CGPointMake(rect.origin.x-0.7 + rect.size.width,rect.origin.y +rect.size.height )};
    CGPoint poinsBottomRightB[] = {CGPointMake(rect.origin.x+ rect.size.width - 15 , rect.origin.y + rect.size.height-0.7),CGPointMake(rect.origin.x + rect.size.width,rect.origin.y + rect.size.height - 0.7 )};
    [self addLine:poinsBottomRightA pointB:poinsBottomRightB ctx:ctx];
    CGContextStrokePath(ctx);
}
- (void)addLine:(CGPoint[])pointA pointB:(CGPoint[])pointB ctx:(CGContextRef)ctx {
    CGContextAddLines(ctx, pointA, 2);
    CGContextAddLines(ctx, pointB, 2);
}
#pragma mark - 功能方法
//#pragma mark 定时器
//- (void)createTimer {
//    self.scanLineTimer =
//    [NSTimer scheduledTimerWithTimeInterval:LINE_SCAN_TIME
//                                     target:self
//                                   selector:@selector(moveUpAndDownLine)
//                                   userInfo:nil
//                                    repeats:YES];
//}
//#pragma mark 移动扫描线
//- (void)moveUpAndDownLine:(CGRect)rect {
//    CGRect readerFrame = self.frame;
//    CGSize viewFinderSize = rect.size;
//    CGRect scanLineframe = self.scanLine.frame;
//    scanLineframe.origin.y = (readerFrame.size.height - viewFinderSize.height)/2;
//    self.scanLine.frame = scanLineframe;
//    self.scanLine.hidden = NO;
//    __weak __typeof(self) weakSelf = self;
//    [UIView animateWithDuration:LINE_SCAN_TIME - 0.05
//                     animations:^{
//                         CGRect scanLineframe = weakSelf.scanLine.frame;
//                         scanLineframe.origin.y =
//                         (readerFrame.size.height + viewFinderSize.height)/2 -
//                         weakSelf.scanLine.frame.size.height;
//                         weakSelf.scanLine.frame = scanLineframe;
//                     }
//                     completion:^(BOOL finished) {
//                         weakSelf.scanLine.hidden = YES;
//                     }];
//    
//}
//设置画笔颜色
- (void)setStrokeColor:(UIColor *)color withContext:(CGContextRef)ctx{
    NSMutableArray *rgbColorArray = [UIColor colorArrayWithHexString:self.scannerRectCornerColor];
    CGFloat r = [rgbColorArray[0] floatValue];
    CGFloat g = [rgbColorArray[1] floatValue];
    CGFloat b = [rgbColorArray[2] floatValue];
    CGContextSetRGBStrokeColor(ctx,r,g,b,1);
}
#pragma mark  - 辅助方法
//将UIColor转换为RGB值
- (NSMutableArray *) changeUIColorToRGB:(UIColor *)color
{
    NSMutableArray *RGBStrValueArr = [[NSMutableArray alloc] init];
    NSString *RGBStr = nil;
    //获得RGB值描述
    NSString *RGBValue = [NSString stringWithFormat:@"%@",color];
    //将RGB值描述分隔成字符串
    NSArray *RGBArr = [RGBValue componentsSeparatedByString:@" "];
    //获取红色值
    float r = [[RGBArr objectAtIndex:1] floatValue] * 255;
    RGBStr = [NSString stringWithFormat:@"%f",r];
    [RGBStrValueArr addObject:RGBStr];
    //获取绿色值
    float g = [[RGBArr objectAtIndex:2] intValue] * 255;
    RGBStr = [NSString stringWithFormat:@"%f",g];
    [RGBStrValueArr addObject:RGBStr];
    //获取蓝色值
    float b = [[RGBArr objectAtIndex:3] intValue] * 255;
    RGBStr = [NSString stringWithFormat:@"%f",b];
    [RGBStrValueArr addObject:RGBStr];
    //返回保存RGB值的数组
    return RGBStrValueArr;
}


@end
