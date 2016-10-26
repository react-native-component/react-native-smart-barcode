

#import <UIKit/UIKit.h>
#import "LineView.h"

@protocol QRCodeScanneDelegate <NSObject>
/**
 *  扫描成功后返回扫描结果
 *
 *  @param result 扫描结果
 */
- (void)didFinshedScanningQRCode:(NSString *)result;

@end

@interface QRCScanner : UIView
/**
 *  提示语
 */
@property (nonatomic,strong)UILabel *noticeInfoLable;
/**
 *  扫描线的颜色,默认红色
 */
@property (nonatomic,strong)UIColor *scanningLieColor;
/**
 *  扫描框边角的颜色，默认红色
 */
@property (nonatomic,strong)UIColor *cornerLineColor;
/**
 *  扫描框的宽高区域，默认(200，200)
 */
@property (nonatomic,assign)CGSize transparentAreaSize;
/**
 *  代理
 */
@property (nonatomic,assign) id<QRCodeScanneDelegate>delegate;
/**
 *  初始化方法
 *
 *  @param QRCScannerView的父view
 *
 *  @return QRCScanner实例
 */
- (instancetype)initQRCScannerWithView:(UIView *)view;
/**
 *  从图片中读取二维码
 *
 *  @param qrimage 一张二维码图片
 *
 *  @return 二维码信息
 */
+ (NSString *)scQRReaderForImage:(UIImage *)qrimage NS_AVAILABLE_IOS(8_0);
@end
