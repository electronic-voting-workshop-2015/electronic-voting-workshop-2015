class CreateCommitments < ActiveRecord::Migration
  def change
      create_table :commitments, id: false do |t|
          t.binary :content
      end
  end
end
