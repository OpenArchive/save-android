import os
from datetime import datetime, timedelta
from github import Github
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail, Email, To, Content

def get_project_stats(repo):
    """Gather project statistics"""
    open_issues = repo.get_issues(state='open')
    closed_issues = repo.get_issues(state='closed')
    
    # Get recent activity
    twenty_four_hours_ago = datetime.now() - timedelta(days=1)
    recent_commits = list(repo.get_commits(since=twenty_four_hours_ago))
    
    stats = {
        'open_issues_count': repo.open_issues_count,
        'closed_issues_count': len(list(closed_issues)),
        'recent_commits': len(recent_commits)
    }
    
    return stats

def create_email_body(repo_name, stats):
    """Create formatted email body"""
    body = f"""
    Daily Project Report for {repo_name}
    Generated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
    
    Project Statistics:
    - Open Issues: {stats['open_issues_count']}
    - Closed Issues: {stats['closed_issues_count']}
    - Commits (Last 24h): {stats['recent_commits']}
    
    View project: https://github.com/{repo_name}
    """
    return body

def send_email(recipient_email, subject, body):
    """Send email using SendGrid"""
    sg = SendGridAPIClient(os.environ['SENDGRID_API_KEY'])
    
    from_email = Email(os.environ['SENDER_EMAIL'])
    to_email = To(recipient_email)
    content = Content("text/plain", body)
    
    mail = Mail(from_email, to_email, subject, content)
    
    try:
        response = sg.client.mail.send.post(request_body=mail.get())
        print(f"Email sent successfully. Status code: {response.status_code}")
    except Exception as e:
        print(f"Error sending email: {e}")

def main():
    # Initialize GitHub client
    g = Github(os.environ['GITHUB_TOKEN'])
    recipient_email = os.environ['RECIPIENT_EMAIL']
    
    # Get repository (assumes running in GitHub Actions context)
    repo_name = os.environ['GITHUB_REPOSITORY']
    repo = g.get_repo(repo_name)
    
    # Generate stats and send email
    stats = get_project_stats(repo)
    body = create_email_body(repo_name, stats)
    send_email(
        recipient_email,
        f"Daily Project Report - {repo_name}",
        body
    )

if __name__ == "__main__":
    main()
