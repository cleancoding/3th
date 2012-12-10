//
//  ChatTableViewCellFactory.m
//  NaverCafe
//
//  Created by 김원겸 on 12. 7. 24..
//  Copyright (c) 2012년 NHN. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "AFNetworking.h"
#import "ChatTableViewCellFactory.h"
#import "ChatRoomViewController.h"
#import "MasterHeaders.h"

#import "Member.h"
#import "MsgList.h"
#import "UserDefaults.h"
#import "NCEtty_Message.h"

@interface ChatMessageHeights : NSObject

+ (CGSize)sizeWithMessage:(NSString *)message font:(UIFont *)font;
+ (void)calcMessageHeightsWithTargetView:(UITextView *)targetView;

@end

#pragma mark - ChatTableViewCell

@implementation ChatTableViewCell
@synthesize chatContentView = _chatContentView,
            delegate = _delegate;

- (void)setChatContentView:(ChatContentView *)chatContentView {
    if (_chatContentView != chatContentView) {
        [_chatContentView release];
        _chatContentView = chatContentView;
        _chatContentView.delegate = self;
        [self.contentView addSubview:_chatContentView];
    }
}

#pragma mark - ChatContentViewDelegate

- (void)chatContentView:(ChatContentView *)chatContentView
        didMemberTapped:(Member *)member
                message:(MsgList *)message {
    // 단순히 포워딩만 수행한다.
    if ([self.delegate respondsToSelector:@selector(chatContentView:didMemberTapped:message:)]) {
        [self.delegate chatContentView:chatContentView didMemberTapped:member message:message];
    }
}

- (void)chatContentView:(ChatContentView *)chatContentView
   didFailMessageTapped:(MsgList *)msgList {
    // 단순히 포워딩만 수행한다.
    if ([self.delegate respondsToSelector:@selector(chatContentView:didFailMessageTapped:)]) {
        [self.delegate chatContentView:chatContentView didFailMessageTapped:msgList];
    }
}

@end

// 대화방 테이블셀 화면.
@interface ChatContentView ()

+ (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL)isShowTimeLine;

@end

@implementation ChatContentView

- (void)dealloc {
    [_message release];
    [_member release];
    [super dealloc];
}

+ (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL)isShowTimeLine {
    
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
								   reason:[NSString stringWithFormat:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)]
								 userInfo:nil];
}

- (void)setLayout {
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
								   reason:[NSString stringWithFormat:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)]
								 userInfo:nil];
}

@end


// 상대방 일반 메세지.
static NSString *kChatNormalContentViewIdentifier = @"ChatNormalContentView";

@interface ChatNormalContentView : ChatContentView
@end

@implementation ChatNormalContentView {
    UILabel     *_nicknameLabel;
    UILabel     *_messageTimeLabel;
	
	UIView  	*_profileView;
	UIImageView *_profileImageView;
    
    UITextView  *_messageTextView;
    UIImageView *_messageBubbleImageView;
    
    UIImageView *_timeLineImageView;
    UILabel     *_timeLineLabel;
}

static UIImage *kPlaceholderProfileImage = nil;

+ (void)initialize {
    kPlaceholderProfileImage = [[UIImage imageNamed:@"chat_img01_member.png"] retain];
}

- (id)init {
    if (self = [super init]) {
		// 타임라인을 만든다.
        _timeLineImageView = [[[UIImageView alloc] init] autorelease];
        _timeLineImageView.hidden = YES;
        [self addSubview:_timeLineImageView];
        
        _timeLineLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0, kMessageMargin, 320, 40)] autorelease];
        _timeLineLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
        _timeLineLabel.font = FONT_B11;
        _timeLineLabel.textColor = COLOR_WHITE;
        _timeLineLabel.backgroundColor = COLOR_CLEAR;
        _timeLineLabel.textAlignment = UITextAlignmentCenter;
        [_timeLineLabel setNumberOfLines:1];
        _timeLineLabel.hidden = YES;
        [self addSubview:_timeLineLabel];
		
        // 닉네임 라벨을 만든다.
        _nicknameLabel.backgroundColor = COLOR_WHITE;
        _nicknameLabel = [[[UILabel alloc] init] autorelease];
        _nicknameLabel.font = kMessageNickNameFont;
        _nicknameLabel.textColor = COLOR_000000;
        _nicknameLabel.alpha = 0.8;
        _nicknameLabel.textAlignment = UITextAlignmentLeft;
        _nicknameLabel.backgroundColor = [UIColor clearColor];
        [self addSubview:_nicknameLabel];

        // 메세지시간 라벨을 만든다.
        _messageTimeLabel = [[[UILabel alloc] init] autorelease];
        _messageTimeLabel.font = kMessageTimeFont;
        _messageTimeLabel.textColor = COLOR_000000;
        _messageTimeLabel.alpha = 0.5;
        _messageTimeLabel.textAlignment = UITextAlignmentLeft;
        _messageTimeLabel.backgroundColor = [UIColor clearColor];
        [self addSubview:_messageTimeLabel];
        
		_profileView = [[[UIView alloc] initWithFrame:CGRectMake(kMessageMargin, kMessageTopMargin, kProfileImageSize+2, kProfileImageSize+2)] autorelease];
        
//		UIView *profileBgView = [[[UIView alloc] init] autorelease];
//		profileBgView.frame = CGRectMake(0, 0, kProfileImageSize + 2, kProfileImageSize + 2);
//		profileBgView.backgroundColor = COLOR_BLACK_30;
//		[_profileView addSubview:profileBgView];
		
		// 프로필 이미지를 만든다.
        _profileImageView = [[[UIImageView alloc] initWithFrame:CGRectMake(1, 1, kProfileImageSize, kProfileImageSize)] autorelease];
//        _profileImageView.layer.masksToBounds = YES;
//        _profileImageView.layer.cornerRadius = 6;
        _profileImageView.userInteractionEnabled = YES;
		
        [_profileView addSubview:_profileImageView];
		
//		UIImageView *profileBgImageView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"chat_img01.png"]] autorelease];
//        profileBgImageView.frame = CGRectMake(0, 0, kProfileImageSize + 1, kProfileImageSize + 1);
//        [_profileView addSubview:profileBgImageView];
        
        UIButton *profileButton = [UIButton buttonWithType:UIButtonTypeCustom];
        profileButton.frame = _profileImageView.frame;
        profileButton.backgroundColor = [UIColor clearColor];
		
        [profileButton addTarget:self
								action:@selector(onProfileImageButton:)
					  forControlEvents:UIControlEventTouchUpInside];
        [_profileImageView addSubview:profileButton];
		
		[self addSubview:_profileView];
		        
        
        // 대화 말풍선을 만든다.
        _messageBubbleImageView = [[[UIImageView alloc] init] autorelease];
        _messageBubbleImageView.image = [[UIImage imageNamed:@"chat_bubble03.png"] stretchableImageWithLeftCapWidth:20 topCapHeight:17];
        _messageBubbleImageView.backgroundColor = COLOR_CLEAR;
        [self addSubview:_messageBubbleImageView];
        
        _messageTextView = [[[UITextView alloc] init] autorelease];
        _messageTextView.font = kMessageFont;
        _messageTextView.backgroundColor = [UIColor clearColor];
        _messageTextView.editable = NO;
        _messageTextView.scrollEnabled = NO;
		_messageTextView.dataDetectorTypes = UIDataDetectorTypePhoneNumber | UIDataDetectorTypeLink;
		
        // UITextView의 Left inset의 0은 +8이다. 메세지 정렬을 위해 -8로 초기화한다.
        _messageTextView.contentInset = UIEdgeInsetsMake(0, -8, 0, 0);
        
        _messageTextView.textAlignment = UITextAlignmentLeft;
        [self addSubview:_messageTextView];
    }
    
    return self;
}

+ (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL) isShowTimeLine {
    CGSize contentSize = [ChatMessageHeights sizeWithMessage:[message content] font:kMessageFont];
    
	int timeLineHeight = 0;
	if (isShowTimeLine) {
		timeLineHeight = 33;
	}
	
    return contentSize.height + 13 + kMessageTopMargin + kMessageBottomMargin + timeLineHeight;
}

- (void)setLayout {
	int timeLineHeight = 0;
     
    if (self.isShowTimeLine) {
		self.isPreviousShowTimeLine = YES;
		timeLineHeight = 33;
		
		_timeLineImageView.image = [[UIImage imageNamed:@"chat_bubble02.png"] stretchableImageWithLeftCapWidth:12 topCapHeight:11.5];
        _timeLineImageView.hidden = NO;
        _timeLineLabel.hidden = NO;        
        
        NSString *timeLineMessage = self.message.createTimeDateString;
        CGSize contentSize = [timeLineMessage sizeWithFont:FONT_B11];
        CGSize ScreenSize = [[UIScreen mainScreen] bounds].size;
        
        int lineCount = 1;
        int contentWidth = contentSize.width;
        
        if (contentWidth > (ScreenSize.width - 10 - 28)) {
            lineCount = 2;
            contentWidth = 282;
        }
        
        CGRect bubbleRect = CGRectMake((self.bounds.size.width - (contentWidth + 28)) / 2,
                                       kMessageTopMargin,
                                       contentWidth + 28,
                                       lineCount * contentSize.height + 7);
        
        _timeLineImageView.frame = bubbleRect;
        
        
        CGRect messageRect = CGRectMake((self.bounds.size.width - contentWidth) / 2,
                                        kMessageTopMargin,
                                        contentWidth,
                                        lineCount * contentSize.height + 7);
        
        _timeLineLabel.text = timeLineMessage;
        _timeLineLabel.frame = messageRect;
		
		CGRect profileRect = _profileView.frame;		
		profileRect.origin.y = timeLineHeight;
		_profileView.frame = profileRect;
        
    } else if (self.isPreviousShowTimeLine) {
		self.isPreviousShowTimeLine = NO;
		_timeLineImageView.image = nil;
        _timeLineImageView.hidden = YES;
        _timeLineLabel.hidden = YES;
        _timeLineLabel.text = @"";
		
		CGRect profileRect = _profileView.frame;
		profileRect.origin.y = kMessageTopMargin;
		_profileView.frame = profileRect;
    }
	
    [_profileImageView setImageWithURL:[NSURL URLWithString:self.message.senderProfileUrl] placeholderImage:kPlaceholderProfileImage];

    NSString *nickName = self.message.senderNickname;
    if ([nickName isEqualToString:@""]) {
        nickName = self.message.senderId;
    }
    
    CGSize nickNameContentSize = [nickName sizeWithFont:kMessageNickNameFont];
    _nicknameLabel.frame = CGRectMake(kMessageMargin + kProfileImageSize + 3 , _profileView.frame.origin.y, nickNameContentSize.width, 15);
    _nicknameLabel.text = nickName;
    
    
    
    _messageTimeLabel.text = self.message.createTimeTimeString;
    CGSize messageTimeContentSize = [_messageTimeLabel.text sizeWithFont:kMessageTimeFont];
    _messageTimeLabel.frame = CGRectMake(kMessageMargin + kProfileImageSize + 3 + _nicknameLabel.frame.size.width + 5 ,
                                         _nicknameLabel.frame.origin.y,
                                         messageTimeContentSize.width,
                                         15);   
    
    
    CGSize contentSize = [ChatMessageHeights sizeWithMessage:[self.message content] font:kMessageFont];
    
    CGRect bubbleRect = CGRectMake(kMessageMargin + kProfileImageSize + 3,
                                   _nicknameLabel.frame.origin.y + _nicknameLabel.frame.size.height,
                                   contentSize.width + 30,
                                   contentSize.height);
    _messageBubbleImageView.frame = bubbleRect;
    
    CGRect messageRect = CGRectMake(kMessageMargin + kProfileImageSize + 3 + 2 + 15,
                                    _nicknameLabel.frame.origin.y + _nicknameLabel.frame.size.height,
                                    contentSize.width + 16,
                                    contentSize.height);
//    _messageTextView.backgroundColor = COLOR_RED_50;
    _messageTextView.text = [self.message content];
    _messageTextView.frame = messageRect;
}

- (void)onProfileImageButton:(id)sender {
    if ([self.delegate respondsToSelector:@selector(chatContentView:didMemberTapped:message:)]) {
        [self.delegate chatContentView:self didMemberTapped:self.member message:[MsgList msgListWithEntityMessage:self.message]];
    }
}

@end


// 내가 작성한 메세지.
static NSString *kChatMeContentViewIdentifier = @"ChatMeContentView";

@interface ChatMeContentView : ChatContentView

@end

@implementation ChatMeContentView {
    UILabel *_messageTimeLabel;
    UITextView *_messageTextView;
    UIImageView *_messageBubbleImageView;
    UIButton *_messageFailButton;
    
    UIImageView *_timeLineImageView;
    UILabel     *_timeLineLabel;
}

- (id)init {
    if (self = [super init]) {
		_timeLineImageView = [[[UIImageView alloc] init] autorelease];
        _timeLineImageView.hidden = YES;
        [self addSubview:_timeLineImageView];
        
        
        _timeLineLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0, kMessageTopMargin, 320, 40)] autorelease];
        _timeLineLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
        _timeLineLabel.font = FONT_B11;
        _timeLineLabel.textColor = COLOR_WHITE;
        _timeLineLabel.backgroundColor = COLOR_CLEAR;
        _timeLineLabel.textAlignment = UITextAlignmentCenter;
        [_timeLineLabel setNumberOfLines:1];
        _timeLineLabel.hidden = YES;
        [self addSubview:_timeLineLabel];
		
        // 메세지시간 라벨을 만든다.
        _messageTimeLabel = [[[UILabel alloc] init] autorelease];
        _messageTimeLabel.frame = CGRectMake(0, kMessageTopMargin, 5, 15);
        _messageTimeLabel.font = kMessageTimeFont;
        _messageTimeLabel.textColor = COLOR_000000;
        _messageTimeLabel.alpha = 0.5;
        _messageTimeLabel.textAlignment = UITextAlignmentRight;
        _messageTimeLabel.backgroundColor = [UIColor clearColor];
        [self addSubview:_messageTimeLabel];
             
        _messageBubbleImageView = [[[UIImageView alloc] init] autorelease];
        _messageBubbleImageView.image = [[UIImage imageNamed:@"chat_bubble04.png"] stretchableImageWithLeftCapWidth:20 topCapHeight:17];
        [self addSubview:_messageBubbleImageView];
    
        
        _messageTextView = [[[UITextView alloc] init] autorelease];
        _messageTextView.font = kMessageFont;
        _messageTextView.backgroundColor = [UIColor clearColor];
        _messageTextView.editable = NO;
        _messageTextView.scrollEnabled = NO;
        _messageTextView.dataDetectorTypes = UIDataDetectorTypePhoneNumber | UIDataDetectorTypeLink;
//		_messageTextView.backgroundColor = COLOR_RED_50;
        // UITextView의 Left inset의 0은 +8이다. 메세지 정렬을 위해 -8로 초기화한다.
        _messageTextView.contentInset = UIEdgeInsetsMake(0, -8, 0, 0);

        
        [self addSubview:_messageTextView];
        
        _messageFailButton = [[UIButton alloc] init];
		_messageFailButton.hidden = YES;
        UIImage* failButtonImage = [UIImage imageNamed:@"chat_icon_fail.png"];
        [_messageFailButton setBackgroundImage:failButtonImage forState:UIControlStateNormal];
        _messageFailButton.frame = CGRectMake(0, 0, failButtonImage.size.height, failButtonImage.size.width);
        [self addSubview:_messageFailButton];
    }
    
    return self;
}

+ (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL) isShowTimeLine {
    CGSize contentSize = [ChatMessageHeights sizeWithMessage:[message content] font:kMessageFont];
    	
	int timeLineHeight = 0;
	if (isShowTimeLine) {
		timeLineHeight = 33;
	}
    
    return contentSize.height - 1 + kMessageTopMargin + kMessageBottomMargin + timeLineHeight;
}

- (void)setLayout {
	int timeLineHeight = 0;
	
    if (self.message.createdTimestamp > 0) {
		if (self.isShowTimeLine) {
			self.isPreviousShowTimeLine = YES;
			timeLineHeight = 33;
			
			_timeLineImageView.image = [[UIImage imageNamed:@"chat_bubble02.png"] stretchableImageWithLeftCapWidth:12 topCapHeight:11.5];
			_timeLineImageView.hidden = NO;
			_timeLineLabel.hidden = NO;
			
			NSString *timeLineMessage = self.message.createTimeDateString;
			CGSize contentSize = [timeLineMessage sizeWithFont:FONT_B11];
			CGSize ScreenSize = [[UIScreen mainScreen] bounds].size;
			
			int lineCount = 1;
			int contentWidth = contentSize.width;
			
			if (contentWidth > (ScreenSize.width - 10 - 28)) {
				lineCount = 2;
				contentWidth = 282;
			}
			
			CGRect bubbleRect = CGRectMake((self.bounds.size.width - (contentWidth + 28)) / 2,
										   kMessageTopMargin,
										   contentWidth + 28,
										   lineCount * contentSize.height + 7);
			
			_timeLineImageView.frame = bubbleRect;
			
			
			CGRect messageRect = CGRectMake((self.bounds.size.width - contentWidth) / 2,
											kMessageTopMargin,
											contentWidth,
											lineCount * contentSize.height + 7);
			
			_timeLineLabel.text = timeLineMessage;
			_timeLineLabel.frame = messageRect;

			
		} else if (self.isPreviousShowTimeLine) {
			self.isPreviousShowTimeLine = NO;
			_timeLineImageView.hidden = YES;
			
			_timeLineImageView.image = nil;
			_timeLineLabel.hidden = YES;
			_timeLineLabel.text = @"";
		}

    }
    
    CGSize contentSize = [ChatMessageHeights sizeWithMessage:[self.message content] font:kMessageFont];
    
    CGRect bubbleRect = CGRectMake(self.bounds.size.width - contentSize.width - 30 - kMessageMargin,
                                   kMessageTopMargin  + timeLineHeight,
                                   contentSize.width + 30,
                                   contentSize.height);
    
    _messageBubbleImageView.frame = bubbleRect;
    
    
    CGRect messageRect = CGRectMake(self.bounds.size.width - contentSize.width - 16 - kMessageMargin,
                                    kMessageTopMargin + timeLineHeight,
                                    contentSize.width + 16,
                                    contentSize.height);
    
    _messageTextView.text = [self.message content];
    _messageTextView.frame = messageRect;

	_messageFailButton.hidden = YES;
	_messageTimeLabel.hidden = YES;
	
	if ([[self.message state] isEqualToString:NC_STR_MSGLIST_NORMAL_STATE]) {
		_messageTimeLabel.text = self.message.createTimeTimeString;
		
		CGSize messageTimeContentSize = [_messageTimeLabel.text sizeWithFont:kMessageTimeFont];
		_messageTimeLabel.frame = CGRectMake(bubbleRect.origin.x - messageTimeContentSize.width - 5,
											 (_messageBubbleImageView.frame.size.height / 2) + timeLineHeight,
											 messageTimeContentSize.width,
											 messageTimeContentSize.height);
		_messageTimeLabel.hidden = NO;
	}
	else if ([[self.message state] isEqualToString:NC_STR_MSGLIST_FAIL_STATE]) {
        CGRect failButtonRect = CGRectMake(bubbleRect.origin.x - _messageFailButton.frame.size.width - 5,
                                           (_messageBubbleImageView.frame.size.height / 2) - 7 + timeLineHeight,
                                           _messageFailButton.frame.size.width,
                                           _messageFailButton.frame.size.height);
       
        _messageFailButton.frame = failButtonRect;

        [_messageFailButton addTarget:self
                                action:@selector(onFailMessageImageButton:)
                      forControlEvents:UIControlEventTouchUpInside];
		_messageFailButton.hidden = NO;
    }
}

- (void)onFailMessageImageButton:(id)sender {
    if ([self.delegate respondsToSelector:@selector(chatContentView:didFailMessageTapped:)]) {
        [self.delegate chatContentView:self didFailMessageTapped:[MsgList msgListWithEntityMessage:self.message]];
    }
}

@end

// 대화상대 퇴장에 사용할 테이블뷰 셀.
static NSString *kChatLeaveContentViewIdentifier = @"ChatLeaveContentView";

@interface ChatLeaveContentView : ChatContentView {
    UILabel *_messageLabel;
    UIImageView *_noticeBgImageView;
}
@end

@implementation ChatLeaveContentView

- (id)init {
    if (self = [super init]) {
        _noticeBgImageView = [[[UIImageView alloc] init] autorelease];
        _noticeBgImageView.image = [[UIImage imageNamed:@"chat_bubble02.png"] stretchableImageWithLeftCapWidth:12 topCapHeight:11.5];
        [self addSubview:_noticeBgImageView];
        
        _messageLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 40)] autorelease];
        _messageLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
        _messageLabel.font = FONT_B11;
        _messageLabel.textColor = COLOR_WHITE;
        _messageLabel.backgroundColor = COLOR_CLEAR;
        _messageLabel.textAlignment = UITextAlignmentCenter;

        [self addSubview:_messageLabel];
    }
    
    return self;
}

+ (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL)isShowLine {
    CGSize contentSize = [self.class sizeWithMessage:[message content] font:FONT_B11];
    return contentSize.height + 7 + (kMessageTopMargin * 2);
}

- (void)setLayout {
    _messageLabel.text = [self.message content];
    
    CGSize contentSize = [self.class sizeWithMessage:_messageLabel.text font:FONT_B11];
    int lineCount = contentSize.height / 14;
    
    _messageLabel.numberOfLines = lineCount;
    _messageLabel.frame = CGRectMake(0, 0, contentSize.width, contentSize.height + 7);
    _messageLabel.center = self.center;

    _noticeBgImageView.frame = CGRectMake(0, 0, contentSize.width + 28, contentSize.height + 7);
    _noticeBgImageView.center = self.center;
}


+ (CGSize)sizeWithMessage:(NSString *)message font:(UIFont *)font {
    CGSize size = [message sizeWithFont:font
                      constrainedToSize:CGSizeMake(287, FLT_MAX)
                          lineBreakMode:UILineBreakModeWordWrap];
    
    return size;
}

@end


@implementation ChatTableViewCellFactory {
    UITableView *_tableView;
    NSMutableDictionary *_registeredContentViewClasses;
}

- (id)initWithTableView:(UITableView *)tableView {
    if (self = [super init]) {
        _tableView = [tableView retain];
        _registeredContentViewClasses = [[NSMutableDictionary alloc] init];
        [self registerContentViewClasses];
    }
    
    return self;
}

- (void)dealloc {
    [_tableView release];
    [_registeredContentViewClasses release];
    [super dealloc];
}

- (void)registerClass:(Class)class forCellWithReuseIdentifier:(NSString *)reuseIdentifier {
    [_registeredContentViewClasses setValue:class forKey:reuseIdentifier];
}

- (void)registerContentViewClasses {
    [self registerClass:[ChatMeContentView class] forCellWithReuseIdentifier:kChatMeContentViewIdentifier];
    [self registerClass:[ChatNormalContentView class] forCellWithReuseIdentifier:kChatNormalContentViewIdentifier];
    [self registerClass:[ChatLeaveContentView class] forCellWithReuseIdentifier:kChatLeaveContentViewIdentifier];    
}

- (NSString *)cellIdentifierWithMessage:(NCEtty_Message *)message {
    if ([message.msgType intValue] == kChatMessageTypeNormal) {
        if ([message.senderId isEqualToString:[CafeUserDefaults loginId]]) {
            return kChatMeContentViewIdentifier;
        }
        else {
            return kChatNormalContentViewIdentifier;
        }
    }
    else if ([message.msgType intValue] == kChatMessageTypeInvite || [message.msgType intValue] == kChatMessageTypeLeave || [message.msgType intValue] == kChatMessageTypeRename || [message.msgType intValue] == kChatMessageTypeChangeMaster) {
        return kChatLeaveContentViewIdentifier;
    } else {
		return kChatLeaveContentViewIdentifier;
	}
    
    return nil;
}

- (ChatContentView *)contentViewWithIdentifier:(NSString *)identifier {    
    Class class = [_registeredContentViewClasses valueForKey:identifier];
    
    if (class != nil) {
        return [[[class alloc] init] autorelease];
    }
    
    return nil;
}

- (CGFloat)heightForCellWithMessage:(NCEtty_Message *)message member:(Member *)member isShowTimeLine:(BOOL)isShowTimeLine{
    NSString *cellIdentifier = [self cellIdentifierWithMessage:message];
    Class class = [_registeredContentViewClasses valueForKey:cellIdentifier];
    
    if (class != nil) {
        return [class heightForCellWithMessage:message member:member isShowTimeLine:isShowTimeLine];
    }

    return 0;
}

- (ChatTableViewCell *)cellWithMessage:(NCEtty_Message *)message
                                member:(Member *)member
                        isShowTimeLine:(BOOL)isShowTimeLine {
    
    NSString *cellIdentifier = [self cellIdentifierWithMessage:message];
    ChatTableViewCell *cell = [_tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    if (cell == nil) {
        cell = [[[ChatTableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier] autorelease];
        cell.chatContentView = [self contentViewWithIdentifier:cellIdentifier];
        cell.chatContentView.opaque = YES;
    }
    cell.frame = CGRectMake(cell.frame.origin.x, cell.frame.origin.y, cell.frame.size.width - 10, cell.frame.size.height);
    
    ChatContentView *chatContentView = cell.chatContentView;
    [chatContentView setMessage:message];
    [chatContentView setMember:member];
    [chatContentView setIsShowTimeLine:isShowTimeLine];
    return cell;
}

@end


@implementation ChatMessageHeights

+ (CGSize)sizeWithMessage:(NSString *)message font:(UIFont *)font {
    CGSize size = [message sizeWithFont:font
                      constrainedToSize:CGSizeMake(192, FLT_MAX)
                          lineBreakMode:UILineBreakModeWordWrap];
    
	
	CGSize lineSize = [@"가" sizeWithFont:font];
	int lineCount = size.height / lineSize.height;

    size.height = 16 + ((lineSize.height +1) * lineCount);
    
    return size;
}

+ (void)calcMessageHeightsWithTargetView:(UITextView *)targetView {
    NSArray *heights = [[self class] messageHeightsWithMessage:@"ㅇ-a" targetView:targetView];
    [[self class] loggingMessageHeights:heights];
}

+ (NSArray *)messageHeightsWithMessage:(NSString *)message targetView:(UITextView *)targetView {
    CGRect originalFrame = targetView.frame;
    BOOL originalScrollEnabled = targetView.scrollEnabled;
    
    targetView.frame = CGRectMake(0, 0, 192, 10);
    targetView.scrollEnabled = YES;
    
    NSMutableArray *results = [NSMutableArray array];
    
    NSString *newText = message;
    targetView.text = newText;
    [results addObject:[NSNumber numberWithFloat:targetView.contentSize.height]];
    
    for (int i = 0; i < 50; i++) {
        newText = [newText stringByAppendingFormat:@"\n%@", message];
        targetView.text = newText;
        [results addObject:[NSNumber numberWithFloat:targetView.contentSize.height]];
    }
    
    targetView.frame = originalFrame;
    targetView.scrollEnabled = originalScrollEnabled;
    
    return results;
}

+ (void)loggingMessageHeights:(NSArray *)heights {
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:heights.count];
    
    for (NSNumber *height in heights) {
        [results addObject:[NSString stringWithFormat:@"@%@.0f", height]];
    }
    
    NSMutableArray *diffs = [NSMutableArray arrayWithCapacity:heights.count];
    for (int i = 1; i < heights.count; i++) {
        CGFloat diff = [[heights objectAtIndex:i - 1] floatValue] - [[heights objectAtIndex:i] floatValue];
        [diffs addObject:[NSString stringWithFormat:@"%f", diff]];
    }
    NaLogTemp(@"%@", diffs);
}

@end