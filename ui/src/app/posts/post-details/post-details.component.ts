import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { Comment } from '../shared/comment.model';
import { Post } from '../shared/post.model';
import { PostService } from '../shared/post.service';

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css'],
})
export class PostDetailsComponent implements OnInit, OnDestroy {
  postId?: string;
  post: Post = { title: '', content: '' };
  comments: Comment[] = [];
  sub?: Subscription;

  constructor(
    private postService: PostService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    console.log('calling ngOnInit::PostDetailsComponent... ');
    this.sub = this.route.params
      .pipe(
        mergeMap((params) => {
          this.postId = params['id'];

          if (this.postId) {
            return forkJoin([
              this.postService.getPost(this.postId),
              this.postService.getCommentsOfPost(this.postId),
            ]);
          }

          throw new Error('post id is required.');
        })
      )
      .subscribe(
        (res: Array<any>) => {
          console.log(res);
          this.post = res[0];
          this.comments = res[1];
        },
        (err) => {
          console.error(err);
        }
      );
  }

  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }
}
