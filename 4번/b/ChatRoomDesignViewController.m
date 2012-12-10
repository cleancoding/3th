//
//  ChatRoomDesignViewController.m
//  NaverCafe
//
//  Created by 김원겸 on 12. 8. 10..
//  Copyright (c) 2012년 NHN. All rights reserved.
//

#import "ChatRoomDesignViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "ChatRoomEventHandler.h"

#import "UIConstant.h"
#import "MasterHeaders.h"
#import "NCCafeEntityController.h"
#import "TNB.h"

static CGFloat kImageTileWidth = 94;
static CGFloat kImageTileHeight = 94;

@interface ImageTileView : UIView {
    UIImageView *_selectionImageView;
}

@property (nonatomic, assign, getter = isSelected) BOOL selected;

- (id)initWithFrame:(CGRect)frame tileImage:(UIImage *)tileImage;

- (void)onSelection;
- (void)offSelection;

@end

@implementation ImageTileView
@synthesize selected = _selected;

- (id)initWithFrame:(CGRect)frame tileImage:(UIImage *)tileImage {
    if (self = [super initWithFrame:frame]) {
        _selected = NO;
        
        UIImageView *tileImageView = [[[UIImageView alloc] initWithImage:tileImage] autorelease];
        tileImageView.frame = self.bounds;
        tileImageView.layer.masksToBounds = YES;
        tileImageView.layer.cornerRadius = 6;
        [self addSubview:tileImageView];
        
        UIImageView *backgroundImageView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"chat_img03.png"]] autorelease];
        backgroundImageView.frame = self.bounds;
        [self addSubview:backgroundImageView];
        
        _selectionImageView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"chat_icon04.png"]] autorelease];
        _selectionImageView.center = CGPointMake(kImageTileWidth - 18, kImageTileWidth - 18);
        [self addSubview:_selectionImageView];
    }
    
    return self;
}

- (void)onSelection {
    _selected = YES;
    _selectionImageView.image = [UIImage imageNamed:@"chat_icon02.png"];
}

- (void)offSelection {
    _selected = NO;
    _selectionImageView.image = [UIImage imageNamed:@"chat_icon04.png"];
}

@end


@interface ImageTilesView : UIView {
    int _selectedTileIndex;
    NSArray */*of NSString*/_imageTileNames;
    NSMutableArray */*of ImageTileView*/_imageTileViews;
    
    UIView *_tileHighlightedView;
}

- (id)initWithImageTileNames:(NSArray */*of NSString*/)imageTileNames
           selectedImageName:(NSString *)selectedImageName;

- (NSString *)selectedTileImageName;

@end

@implementation ImageTilesView

- (id)initWithImageTileNames:(NSArray */*of NSString*/)imageTileNames
           selectedImageName:(NSString *)selectedImageName {
    if (self = [super init]) {
        _selectedTileIndex = [imageTileNames indexOfObject:selectedImageName];
        _imageTileNames = [imageTileNames retain];
        
        [self makeTileImageViews:[self loadImageTileNames]];
        [self adjustSectionHighlightWithIndex:_selectedTileIndex];
    }
    
    return self;
}

- (void)dealloc {
    [_imageTileNames release];
    [_imageTileViews release];
    [super dealloc];
}

- (NSArray */*of UIImage*/)loadImageTileNames {
    NSMutableArray *results = [[NSMutableArray alloc] init];
    
    for (NSString *imageTileName in _imageTileNames) {
        [results addObject:[UIImage imageNamed:imageTileName]];
    }
    
    return results;
}

- (CGRect)tileFrameWithIndex:(int)index {
    int x = index % 3;
    int y = index / 3;
    return CGRectMake(x * (kImageTileWidth + 9) + 10, y * (kImageTileHeight + 9) + 10, kImageTileWidth, kImageTileHeight);
}

- (void)makeTileImageViews:(NSArray */*of UIImage*/)imageTiles {
    _imageTileViews = [[NSMutableArray alloc] init];
    
    for (int index = 0; index < imageTiles.count; index++) {
        UIImage *backgroundImage = [imageTiles objectAtIndex:index];
        CGRect frame = [self tileFrameWithIndex:index];
        
        ImageTileView *imageTileView = [[[ImageTileView alloc] initWithFrame:frame tileImage:backgroundImage] autorelease];
        imageTileView.tag = index;
        
        [self addSubview:imageTileView];
        [_imageTileViews addObject:imageTileView];
    }
}

- (void)adjustSectionHighlightWithIndex:(int)index {
    for (ImageTileView *imageTileView in _imageTileViews) {
        if (index == imageTileView.tag) {
            [imageTileView onSelection];
        }
        else {
            [imageTileView offSelection];
        }
    }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    if ([touch.view isKindOfClass:[ImageTileView class]]) {
        _selectedTileIndex = touch.view.tag;
        [self adjustSectionHighlightWithIndex:_selectedTileIndex];
    }
}

- (NSString *)selectedTileImageName {
    return [_imageTileNames objectAtIndex:_selectedTileIndex];
}

@end


@interface ChatRoomDesignViewController () {
    ChatRoomEventHandler *_chatRoomEventHandler;
    ImageTilesView *_imageTilesView;
}

@end

@implementation ChatRoomDesignViewController

static NSMutableArray *kBackgroundImageNames = nil;

+ (void)initialize {
    kBackgroundImageNames = [[NSMutableArray alloc] init];
	[kBackgroundImageNames addObject:@"chat_skin09.png"];
    [kBackgroundImageNames addObject:@"chat_skin02.png"];
    [kBackgroundImageNames addObject:@"chat_skin03.png"];
    [kBackgroundImageNames addObject:@"chat_skin04.png"];
	[kBackgroundImageNames addObject:@"chat_skin05.png"];
    [kBackgroundImageNames addObject:@"chat_skin06.png"];
    [kBackgroundImageNames addObject:@"chat_skin07.png"];
    [kBackgroundImageNames addObject:@"chat_skin08.png"];
    [kBackgroundImageNames addObject:@"chat_skin01.png"];
}

- (id)initWithChatRoomEventHandler:(ChatRoomEventHandler *)chatRoomEventHandler {
    if (self = [super init]) {
        _chatRoomEventHandler = [chatRoomEventHandler retain];
        self.view.backgroundColor = [UIColor whiteColor];
        
        [self makeTNB];
        
        // 배경이미지 타일을 만든다.
        _imageTilesView = [[[ImageTilesView alloc] initWithImageTileNames:kBackgroundImageNames
                                                        selectedImageName:_chatRoomEventHandler.backgroundImageName] autorelease];
        _imageTilesView.frame = CGRectMake(0, NC_CHAT_MAIN_TNB_HEIGHT, 320, NC_CHAT_INVITE_CONTENT_HEIGHT);
        [self.view addSubview:_imageTilesView];
        
        // "배경으로 설정" 버튼을 만든다.
        UIView *backgroundView = NaMakeView(CGRectMake(0, NC_CHAT_INVITE_CONTENT_HEIGHT + 44, NC_CHAT_MAIN_CONTENT_WIDTH, NC_CHAT_INVITE_BOTTOM_HEIGHT),
                                            [UIColor colorWithPatternImage:[UIImage imageNamed:@"chat_sub_bg.png"]]);
        [self.view addSubview:backgroundView];
        
        UIButton *backgroundSettingButton = NaMakeButtonForImage(CGRectMake(0, 0, 141 , 38), [UIImage imageNamed:@"chat_btn_background_pressed.png"], [UIImage imageNamed:@"chat_btn_background_normal.png"]);
        backgroundSettingButton.center = backgroundView.center;
        
        [backgroundSettingButton addTarget:self
                                    action:@selector(onBackgroundSettingButton:)
                          forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:backgroundSettingButton];
    }
    
    return self;
}

- (void)dealloc {
    [_chatRoomEventHandler release];
    [super dealloc];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)makeTNB {
    TNB *_tnbView = [[[TNB alloc] initWithFrame:NC_TNB_FRAME] autorelease];
    [_tnbView setBackgroundImage:[ThemeUtility tnbNewBackground]];
    [_tnbView setTitle:@"배경 꾸미기"];
    
    // 닫기 버튼 추가.
    [_tnbView setLeftButtonWithDimedImage:[UIImage imageNamed:@"title_btn_close_normal.png"]
                               dimedImage:[UIImage imageNamed:@"title_btn_close_pressed.png"]
                                   target:self
                                   action:@selector(onCloseButton:)
                                withStyle:YES];
    
    [self.view addSubview:_tnbView];
}

#pragma mark - 버튼 이벤트 관련

- (void)onCloseButton:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

- (void)onBackgroundSettingButton:(id)sender {
    _chatRoomEventHandler.backgroundImageName = [_imageTilesView selectedTileImageName];
    [[NCCafeEntityController getSharedInstance] updateRoom:_chatRoomEventHandler.roomId
                                       backgroundImageName:_chatRoomEventHandler.backgroundImageName];
    [self onCloseButton:nil];
}

@end
